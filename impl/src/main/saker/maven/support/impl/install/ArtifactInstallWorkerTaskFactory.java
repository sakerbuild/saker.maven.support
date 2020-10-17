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
package saker.maven.support.impl.install;

import java.io.Externalizable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

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
import saker.maven.support.api.ArtifactCoordinates;
import saker.maven.support.api.MavenOperationConfiguration;
import saker.maven.support.api.install.ArtifactInstallWorkerTaskOutput;
import saker.maven.support.impl.MavenImplUtils;
import saker.maven.support.main.install.InstallArtifactsTaskFactory;
import saker.maven.support.thirdparty.org.eclipse.aether.DefaultRepositorySystemSession;
import saker.maven.support.thirdparty.org.eclipse.aether.RepositorySystem;
import saker.maven.support.thirdparty.org.eclipse.aether.artifact.Artifact;
import saker.maven.support.thirdparty.org.eclipse.aether.artifact.DefaultArtifact;
import saker.maven.support.thirdparty.org.eclipse.aether.impl.DefaultServiceLocator;
import saker.maven.support.thirdparty.org.eclipse.aether.installation.InstallRequest;
import saker.maven.support.thirdparty.org.eclipse.aether.repository.LocalRepository;
import saker.maven.support.thirdparty.org.eclipse.aether.repository.LocalRepositoryManager;
import saker.std.api.util.SakerStandardUtils;

public class ArtifactInstallWorkerTaskFactory
		implements TaskFactory<ArtifactInstallWorkerTaskOutput>, Task<ArtifactInstallWorkerTaskOutput>, Externalizable {

	private static final long serialVersionUID = 1L;

	protected MavenOperationConfiguration configuration;
	protected ArtifactCoordinates coordinates;
	protected SakerPath artifactPath;

	/**
	 * For {@link Externalizable}.
	 */
	public ArtifactInstallWorkerTaskFactory() {
	}

	public ArtifactInstallWorkerTaskFactory(MavenOperationConfiguration configuration, ArtifactCoordinates coordinates,
			SakerPath artifactPath) {
		Objects.requireNonNull(configuration, "configuration");
		Objects.requireNonNull(coordinates, "coordinates");
		Objects.requireNonNull(artifactPath, "artifact path");
		//we dont need the remote repositories, clear them.
		this.configuration = MavenOperationConfiguration.builder(configuration).setRepositories(Collections.emptySet())
				.build();
		this.coordinates = coordinates;
		this.artifactPath = artifactPath;
	}

	public TaskIdentifier createTaskIdentifier() {
		return new ArtifactInstallWorkerTaskIdentifier(configuration, coordinates);
	}

	@Override
	public Task<? extends ArtifactInstallWorkerTaskOutput> createTask(ExecutionContext executioncontext) {
		return this;
	}

	//suppress the unused FileLock warning
	@SuppressWarnings("try")
	@Override
	public ArtifactInstallWorkerTaskOutput run(TaskContext taskcontext) throws Exception {
		taskcontext.setStandardOutDisplayIdentifier(InstallArtifactsTaskFactory.TASK_NAME);
		MavenImplUtils.reportConfgurationBuildTrace(configuration);

		SakerFile artifactfile = artifactPath == null ? null
				: taskcontext.getTaskUtilities().resolveFileAtPath(artifactPath);
		if (artifactfile == null) {
			taskcontext.reportInputFileDependency(null, artifactPath, CommonTaskContentDescriptors.IS_NOT_FILE);
			taskcontext.abortExecution(new FileNotFoundException("Artifact not found: " + artifactPath));
			return null;
		}
		MirroredFileContents artifactmirrorresult = taskcontext.getTaskUtilities()
				.mirrorFileAtPathContents(artifactPath);
		taskcontext.reportInputFileDependency(null, artifactPath, artifactmirrorresult.getContents());
		Path artifactmirrorpath = artifactmirrorresult.getPath();

		MavenOperationConfiguration config = this.configuration;

		SakerPath repositorybasedir = MavenImplUtils.getRepositoryBaseDirectoryDefaulted(taskcontext, config);

		SakerPath lockfilepath = MavenImplUtils.getAccessLockFilePathInRepository(repositorybasedir);
		Path lockfilelocalpath = LocalFileProvider.toRealPath(lockfilepath);

		LocalFileProvider localfp = LocalFileProvider.getInstance();
		localfp.createDirectories(lockfilelocalpath.getParent());

		DefaultServiceLocator serviceLocator = MavenImplUtils.getDefaultServiceLocator();

		RepositorySystem reposystem = serviceLocator.getService(RepositorySystem.class);

		DefaultRepositorySystemSession reposession = MavenImplUtils.createNewSession(taskcontext, config);

		LocalRepository localrepository = new LocalRepository(repositorybasedir.toString());
		LocalRepositoryManager localrepomanager = reposystem.newLocalRepositoryManager(reposession, localrepository);
		reposession.setLocalRepositoryManager(localrepomanager);

		reposession.setReadOnly();

		SakerPath installresultartifactpath = null;
		synchronized (MavenImplUtils.getLocalRepositoryAccessSyncLock(lockfilepath)) {
			try (FileChannel lockchannel = FileChannel.open(lockfilelocalpath, StandardOpenOption.CREATE,
					StandardOpenOption.WRITE);
					FileLock lock = lockchannel.lock(0, Long.MAX_VALUE, false)) {

				InstallRequest request = new InstallRequest();
				Artifact artifact = null;
				artifact = new DefaultArtifact(coordinates.getGroupId(), coordinates.getArtifactId(),
						coordinates.getClassifier(), coordinates.getExtension(), coordinates.getVersion())
								.setFile(artifactmirrorpath.toFile());
				request.addArtifact(artifact);

				reposystem.install(reposession, request);

				UUID cduniqueness = UUID.randomUUID();
				if (artifact != null) {
					String localpath = reposession.getLocalRepositoryManager().getPathForLocalArtifact(artifact);
					installresultartifactpath = repositorybasedir.resolve(localpath);
					taskcontext.getTaskUtilities().getReportExecutionDependency(
							SakerStandardUtils.createLocalFileContentDescriptorExecutionProperty(
									installresultartifactpath, cduniqueness));
				}
			}
		}

		return new ArtifactInstallWorkerTaskOutputImpl(coordinates, installresultartifactpath);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(configuration);
		out.writeObject(coordinates);
		out.writeObject(artifactPath);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		configuration = (MavenOperationConfiguration) in.readObject();
		coordinates = (ArtifactCoordinates) in.readObject();
		artifactPath = (SakerPath) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((artifactPath == null) ? 0 : artifactPath.hashCode());
		result = prime * result + ((configuration == null) ? 0 : configuration.hashCode());
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
		ArtifactInstallWorkerTaskFactory other = (ArtifactInstallWorkerTaskFactory) obj;
		if (artifactPath == null) {
			if (other.artifactPath != null)
				return false;
		} else if (!artifactPath.equals(other.artifactPath))
			return false;
		if (configuration == null) {
			if (other.configuration != null)
				return false;
		} else if (!configuration.equals(other.configuration))
			return false;
		if (coordinates == null) {
			if (other.coordinates != null)
				return false;
		} else if (!coordinates.equals(other.coordinates))
			return false;
		return true;
	}

	private static final class ArtifactInstallWorkerTaskOutputImpl
			implements ArtifactInstallWorkerTaskOutput, Externalizable {
		private static final long serialVersionUID = 1L;

		private ArtifactCoordinates coordinates;
		private SakerPath artifactLocalPath;

		/**
		 * For {@link Externalizable}.
		 */
		public ArtifactInstallWorkerTaskOutputImpl() {
		}

		public ArtifactInstallWorkerTaskOutputImpl(ArtifactCoordinates coordinates, SakerPath artifactLocalPath) {
			this.coordinates = coordinates;
			this.artifactLocalPath = artifactLocalPath;
		}

		@Override
		public SakerPath getArtifactLocalPath() {
			return artifactLocalPath;
		}

		@Override
		public ArtifactCoordinates getCoordinates() {
			return coordinates;
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeObject(coordinates);
			out.writeObject(artifactLocalPath);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			coordinates = (ArtifactCoordinates) in.readObject();
			artifactLocalPath = (SakerPath) in.readObject();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((artifactLocalPath == null) ? 0 : artifactLocalPath.hashCode());
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
			ArtifactInstallWorkerTaskOutputImpl other = (ArtifactInstallWorkerTaskOutputImpl) obj;
			if (artifactLocalPath == null) {
				if (other.artifactLocalPath != null)
					return false;
			} else if (!artifactLocalPath.equals(other.artifactLocalPath))
				return false;
			if (coordinates == null) {
				if (other.coordinates != null)
					return false;
			} else if (!coordinates.equals(other.coordinates))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "[" + (coordinates != null ? "coordinates=" + coordinates + ", " : "")
					+ (artifactLocalPath != null ? "artifactLocalPath=" + artifactLocalPath : "") + "]";
		}

	}
}
