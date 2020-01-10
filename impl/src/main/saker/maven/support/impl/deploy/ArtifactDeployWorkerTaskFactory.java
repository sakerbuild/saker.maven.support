/*
 * Copyright (C) 2020 Bence Sipka
 *
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package saker.maven.support.impl.deploy;

import java.io.Externalizable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import saker.build.file.SakerFile;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.CommonTaskContentDescriptors;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskExecutionUtilities.MirroredFileContents;
import saker.build.task.TaskFactory;
import saker.build.task.identifier.TaskIdentifier;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.build.util.property.BuildTimeExecutionProperty;
import saker.maven.support.api.ArtifactCoordinates;
import saker.maven.support.api.MavenOperationConfiguration.RepositoryConfiguration;
import saker.maven.support.api.deploy.ArtifactDeployWorkerTaskOutput;
import saker.maven.support.impl.MavenImplUtils;
import saker.maven.support.main.deploy.DeployArtifactsTaskFactory;
import saker.maven.support.thirdparty.org.eclipse.aether.DefaultRepositorySystemSession;
import saker.maven.support.thirdparty.org.eclipse.aether.RepositorySystem;
import saker.maven.support.thirdparty.org.eclipse.aether.artifact.Artifact;
import saker.maven.support.thirdparty.org.eclipse.aether.artifact.DefaultArtifact;
import saker.maven.support.thirdparty.org.eclipse.aether.deployment.DeployRequest;
import saker.maven.support.thirdparty.org.eclipse.aether.impl.DefaultServiceLocator;
import saker.maven.support.thirdparty.org.eclipse.aether.repository.LocalRepository;
import saker.maven.support.thirdparty.org.eclipse.aether.repository.LocalRepositoryManager;

public class ArtifactDeployWorkerTaskFactory
		implements TaskFactory<ArtifactDeployWorkerTaskOutput>, Task<ArtifactDeployWorkerTaskOutput>, Externalizable {

	private static final long serialVersionUID = 1L;

	protected RepositoryConfiguration repositoryConfiguration;
	protected ArtifactCoordinates coordinates;
	protected Map<String, SakerPath> artifacts;

	/**
	 * For {@link Externalizable}.
	 */
	public ArtifactDeployWorkerTaskFactory() {
	}

	public ArtifactDeployWorkerTaskFactory(RepositoryConfiguration repositoryConfiguration,
			ArtifactCoordinates coordinates, Map<String, SakerPath> artifacts) {
		Objects.requireNonNull(repositoryConfiguration, "repository configuration");
		Objects.requireNonNull(coordinates, "coordinates");
		Objects.requireNonNull(artifacts, "artifacts");

		if (coordinates.getExtension() != null) {
			throw new IllegalArgumentException("Deploy coordinates must not have extension.");
		}
		if (coordinates.getClassifier() != null) {
			throw new IllegalArgumentException("Deploy coordinates must not have classifier.");
		}

		this.repositoryConfiguration = repositoryConfiguration;
		this.coordinates = coordinates;
		this.artifacts = ImmutableUtils.makeImmutableNavigableMap(artifacts);
	}

	public TaskIdentifier createTaskIdentifier() {
		return new ArtifactDeployWorkerTaskIdentifier(repositoryConfiguration, coordinates);
	}

	private static Entry<String, String> resolveSpecifier(String spec) {
		int cidx = spec.indexOf(':');
		if (cidx < 0) {
			spec = spec.trim();
			if (spec.isEmpty()) {
				return ImmutableUtils.makeImmutableMapEntry(null, "jar");
			}
			return ImmutableUtils.makeImmutableMapEntry(null, spec);
		}
		int lcidx = spec.lastIndexOf(':');
		if (cidx != lcidx) {
			throw new IllegalArgumentException("Multiple ':' in specifier: " + spec);
		}
		String classifier = spec.substring(0, cidx).trim();
		String extension = spec.substring(cidx + 1).trim();
		if (extension.isEmpty()) {
			extension = "jar";
		}
		if (classifier.isEmpty()) {
			classifier = null;
		}
		return ImmutableUtils.makeImmutableMapEntry(classifier, extension);
	}

	@SuppressWarnings("try")
	@Override
	public ArtifactDeployWorkerTaskOutput run(TaskContext taskcontext) throws Exception {
		if (artifacts.isEmpty()) {
			return new ArtifactDeployWorkerTaskOutputImpl(coordinates);
		}
		Map<Entry<String, String>, Path> mirrorpaths = new LinkedHashMap<>();
		for (Entry<String, SakerPath> entry : artifacts.entrySet()) {
			SakerPath artifactpath = entry.getValue();
			SakerFile artifactfile = taskcontext.getTaskUtilities().resolveFileAtPath(artifactpath);
			if (artifactfile == null) {
				taskcontext.reportInputFileDependency(null, artifactpath, CommonTaskContentDescriptors.IS_NOT_FILE);
				taskcontext.abortExecution(new FileNotFoundException("Artifact not found: " + artifactpath));
				return null;
			}
			MirroredFileContents artifactmirrorresult = taskcontext.getTaskUtilities()
					.mirrorFileAtPathContents(artifactpath);
			taskcontext.reportInputFileDependency(null, artifactpath, artifactmirrorresult.getContents());
			Path artifactmirrorpath = artifactmirrorresult.getPath();
			Entry<String, String> specifier = resolveSpecifier(entry.getKey());
			Path prev = mirrorpaths.put(specifier, artifactmirrorpath);
			if (prev != null) {
				taskcontext.abortExecution(new IllegalArgumentException(
						"Multiple artifact specifiers that resolved to " + Objects.toString(specifier.getKey(), "")
								+ ":" + specifier.getValue() + " in: " + artifacts.keySet()));
				return null;
			}
		}

		SakerPath repoworkspacedir = taskcontext.getTaskBuildDirectoryPath()
				.resolve(DeployArtifactsTaskFactory.TASK_NAME, "maven-deploy-workspace");
		SakerPath repositorybasedir = SakerPath
				.valueOf(taskcontext.getExecutionContext().toMirrorPath(repoworkspacedir));

		SakerPath lockfilepath = MavenImplUtils.getAccessLockFilePathInRepository(repositorybasedir);
		Path lockfilelocalpath = LocalFileProvider.toRealPath(lockfilepath);

		LocalFileProvider localfp = LocalFileProvider.getInstance();
		localfp.createDirectories(lockfilelocalpath.getParent());

		DefaultServiceLocator serviceLocator = MavenImplUtils.getDefaultServiceLocator();

		RepositorySystem reposystem = serviceLocator.getService(RepositorySystem.class);

		DefaultRepositorySystemSession reposession = MavenImplUtils.createNewSession(taskcontext, null);

		LocalRepository localrepository = new LocalRepository(repositorybasedir.toString());
		LocalRepositoryManager localrepomanager = reposystem.newLocalRepositoryManager(reposession, localrepository);
		reposession.setLocalRepositoryManager(localrepomanager);

		reposession.setReadOnly();

		synchronized (MavenImplUtils.getLocalRepositoryAccessSyncLock(lockfilepath)) {
			try (FileChannel lockchannel = FileChannel.open(lockfilelocalpath, StandardOpenOption.CREATE,
					StandardOpenOption.WRITE);
					FileLock lock = lockchannel.lock(0, Long.MAX_VALUE, false)) {

				DeployRequest request = new DeployRequest();
				request.setRepository(MavenImplUtils.createRemoteRepository(repositoryConfiguration));

				for (Entry<Entry<String, String>, Path> entry : mirrorpaths.entrySet()) {
					Entry<String, String> specifier = entry.getKey();
					String classifier = specifier.getKey();
					String extension = specifier.getValue();

					Artifact artifact = new DefaultArtifact(coordinates.getGroupId(), coordinates.getArtifactId(),
							classifier, extension, coordinates.getVersion()).setFile(entry.getValue().toFile());
					request.addArtifact(artifact);
				}

				reposystem.deploy(reposession, request);
			}
		}

		//report build time dependency to always reinvoke the deploy task.
		taskcontext.reportExecutionDependency(BuildTimeExecutionProperty.INSTANCE, null);

		return new ArtifactDeployWorkerTaskOutputImpl(coordinates);
	}

	@Override
	public Task<? extends ArtifactDeployWorkerTaskOutput> createTask(ExecutionContext executioncontext) {
		return this;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(repositoryConfiguration);
		out.writeObject(coordinates);
		SerialUtils.writeExternalMap(out, artifacts);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		repositoryConfiguration = (RepositoryConfiguration) in.readObject();
		coordinates = (ArtifactCoordinates) in.readObject();
		artifacts = SerialUtils.readExternalSortedImmutableNavigableMap(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((artifacts == null) ? 0 : artifacts.hashCode());
		result = prime * result + ((coordinates == null) ? 0 : coordinates.hashCode());
		result = prime * result + ((repositoryConfiguration == null) ? 0 : repositoryConfiguration.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ArtifactDeployWorkerTaskFactory other = (ArtifactDeployWorkerTaskFactory) obj;
		if (artifacts == null) {
			if (other.artifacts != null)
				return false;
		} else if (!artifacts.equals(other.artifacts))
			return false;
		if (coordinates == null) {
			if (other.coordinates != null)
				return false;
		} else if (!coordinates.equals(other.coordinates))
			return false;
		if (repositoryConfiguration == null) {
			if (other.repositoryConfiguration != null)
				return false;
		} else if (!repositoryConfiguration.equals(other.repositoryConfiguration))
			return false;
		return true;
	}

	private static final class ArtifactDeployWorkerTaskOutputImpl
			implements ArtifactDeployWorkerTaskOutput, Externalizable {
		private static final long serialVersionUID = 1L;

		private ArtifactCoordinates coordinates;

		/**
		 * For {@link Externalizable}.
		 */
		public ArtifactDeployWorkerTaskOutputImpl() {
		}

		public ArtifactDeployWorkerTaskOutputImpl(ArtifactCoordinates coordinates) {
			this.coordinates = coordinates;
		}

		@Override
		public ArtifactCoordinates getCoordinates() {
			return coordinates;
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeObject(coordinates);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			coordinates = (ArtifactCoordinates) in.readObject();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((coordinates == null) ? 0 : coordinates.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ArtifactDeployWorkerTaskOutputImpl other = (ArtifactDeployWorkerTaskOutputImpl) obj;
			if (coordinates == null) {
				if (other.coordinates != null)
					return false;
			} else if (!coordinates.equals(other.coordinates))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "[" + (coordinates != null ? "coordinates=" + coordinates : "") + "]";
		}

	}
}
