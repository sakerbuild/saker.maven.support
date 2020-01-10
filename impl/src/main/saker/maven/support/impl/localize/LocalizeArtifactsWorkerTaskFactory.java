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
package saker.maven.support.impl.localize;

import java.io.Externalizable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import saker.build.file.content.ContentDescriptor;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.task.identifier.TaskIdentifier;
import saker.build.task.utils.StructuredTaskResult;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.maven.support.api.ArtifactCoordinates;
import saker.maven.support.api.MavenOperationConfiguration;
import saker.maven.support.api.localize.ArtifactLocalizationTaskOutput;
import saker.maven.support.impl.ArtifactContentDescriptorExecutionProperty;
import saker.maven.support.impl.ArtifactUtils;
import saker.maven.support.impl.MavenImplUtils;
import saker.maven.support.impl.download.DownloadFailedStructuredTaskResult;
import saker.maven.support.main.download.DownloadArtifactsTaskFactory;
import saker.maven.support.thirdparty.org.eclipse.aether.DefaultRepositorySystemSession;
import saker.maven.support.thirdparty.org.eclipse.aether.RepositorySystem;
import saker.maven.support.thirdparty.org.eclipse.aether.artifact.Artifact;
import saker.maven.support.thirdparty.org.eclipse.aether.impl.DefaultServiceLocator;
import saker.maven.support.thirdparty.org.eclipse.aether.repository.LocalRepository;
import saker.maven.support.thirdparty.org.eclipse.aether.repository.LocalRepositoryManager;
import saker.maven.support.thirdparty.org.eclipse.aether.repository.RemoteRepository;
import saker.maven.support.thirdparty.org.eclipse.aether.resolution.ArtifactRequest;
import saker.maven.support.thirdparty.org.eclipse.aether.resolution.ArtifactResolutionException;
import saker.maven.support.thirdparty.org.eclipse.aether.resolution.ArtifactResult;

public class LocalizeArtifactsWorkerTaskFactory implements TaskFactory<ArtifactLocalizationTaskOutput>,
		Task<ArtifactLocalizationTaskOutput>, Externalizable, TaskIdentifier {
	//TODO this class has a lot of common with DownloadArtifactsWorkerTaskFactory
	private static final long serialVersionUID = 1L;

	protected MavenOperationConfiguration configuration;
	protected Set<? extends ArtifactCoordinates> artifacts;

	/**
	 * For {@link Externalizable}.
	 */
	public LocalizeArtifactsWorkerTaskFactory() {
	}

	public LocalizeArtifactsWorkerTaskFactory(MavenOperationConfiguration operationConfiguration,
			Set<? extends ArtifactCoordinates> artifacts) {
		Objects.requireNonNull(artifacts, "artifacts");
		if (operationConfiguration == null) {
			operationConfiguration = MavenOperationConfiguration.defaults();
		}
		this.configuration = operationConfiguration;
		this.artifacts = ImmutableUtils.makeImmutableLinkedHashSet(artifacts);
	}

	//suppress the unused FileLock warning
	@SuppressWarnings("try")
	@Override
	public ArtifactLocalizationTaskOutput run(TaskContext taskcontext) throws Exception {
		taskcontext.setStandardOutDisplayIdentifier(DownloadArtifactsTaskFactory.TASK_NAME);

		MavenOperationConfiguration config = this.configuration;
		List<RemoteRepository> repositories = MavenImplUtils.createRemoteRepositories(config);

		Map<ArtifactRequest, ArtifactCoordinates> artifactrequests = new LinkedHashMap<>();
		for (ArtifactCoordinates acoords : artifacts) {
			artifactrequests.put(new ArtifactRequest(ArtifactUtils.toArtifact(acoords), repositories, null), acoords);
		}
		Map<ArtifactCoordinates, StructuredTaskResult> coordinateResults = new LinkedHashMap<>();
		final List<Throwable> failexceptions = new ArrayList<>();
		try {
			SakerPath repositorybasedir = MavenImplUtils.getRepositoryBaseDirectoryDefaulted(taskcontext, config);

			SakerPath lockfilepath = MavenImplUtils.getAccessLockFilePathInRepository(repositorybasedir);
			Path lockfilelocalpath = LocalFileProvider.toRealPath(lockfilepath);

			LocalFileProvider localfp = LocalFileProvider.getInstance();
			localfp.createDirectories(lockfilelocalpath.getParent());

			DefaultServiceLocator serviceLocator = MavenImplUtils.getDefaultServiceLocator();

			RepositorySystem reposystem = serviceLocator.getService(RepositorySystem.class);

			DefaultRepositorySystemSession reposession = MavenImplUtils.createNewSession(taskcontext, config);

			LocalRepository localrepository = new LocalRepository(repositorybasedir.toString());
			LocalRepositoryManager localrepomanager = reposystem.newLocalRepositoryManager(reposession,
					localrepository);
			reposession.setLocalRepositoryManager(localrepomanager);

			reposession.setReadOnly();

			synchronized (MavenImplUtils.getLocalRepositoryAccessSyncLock(lockfilepath)) {
				try (FileChannel lockchannel = FileChannel.open(lockfilelocalpath, StandardOpenOption.CREATE,
						StandardOpenOption.WRITE);
						FileLock lock = lockchannel.lock(0, Long.MAX_VALUE, false)) {
					//taskcontext.invalidate() is called on the download locations by the repository listener

					List<ArtifactResult> resolvedartifacts;
					try {
						resolvedartifacts = reposystem.resolveArtifacts(reposession, artifactrequests.keySet());
					} catch (ArtifactResolutionException e) {
						failexceptions.add(e);
						resolvedartifacts = e.getResults();
					}

					handleArtifactResults(taskcontext, artifactrequests, coordinateResults, failexceptions,
							repositorybasedir, resolvedartifacts, localrepomanager);
				}
			}
		} catch (Exception e) {
			failexceptions.add(e);
		} finally {
			//if any exception happens, and we fail to start all the requested tasks, start them for the remaining as well
			if (!artifactrequests.isEmpty()) {
				Iterator<Entry<ArtifactRequest, ArtifactCoordinates>> it = artifactrequests.entrySet().iterator();
				do {
					Entry<ArtifactRequest, ArtifactCoordinates> entry = it.next();
					ArtifactCoordinates acoords = entry.getValue();

					coordinateResults.put(acoords,
							new DownloadFailedStructuredTaskResult("Failed to download " + acoords, failexceptions));
				} while (it.hasNext());
			}
		}
		if (!ObjectUtils.isNullOrEmpty(failexceptions)) {
			for (Throwable e : failexceptions) {
				taskcontext.getTaskUtilities().reportIgnoredException(e);
			}
		}

		return new ArtifactLocalizationTaskOutputImpl(config, coordinateResults);
	}

	private static void handleArtifactResults(TaskContext taskcontext,
			Map<ArtifactRequest, ArtifactCoordinates> artifactrequests,
			Map<ArtifactCoordinates, StructuredTaskResult> coordinateResults, final List<Throwable> failexceptions,
			SakerPath repositorybasedir, List<ArtifactResult> resolvedartifacts,
			LocalRepositoryManager localrepomanager) throws AssertionError {
		if (ObjectUtils.isNullOrEmpty(resolvedartifacts)) {
			return;
		}
		UUID cduniqueness = UUID.randomUUID();
		for (ArtifactResult result : resolvedartifacts) {
			ArtifactRequest request = result.getRequest();
			ArtifactCoordinates acoords = artifactrequests.remove(request);
			if (acoords == null) {
				throw new AssertionError(
						"Internal error: failed to match artifact download requests to artifact coordinates.");
			}
			Artifact resultartifact = result.getArtifact();

			List<Exception> exceptions = result.getExceptions();
			if (!ObjectUtils.isNullOrEmpty(exceptions)) {
				failexceptions.addAll(exceptions);
			}
			File file;
			if (resultartifact == null) {
				Artifact requestartifact = request.getArtifact();

				installLocalizationFailedDependencies(taskcontext, repositorybasedir, localrepomanager, cduniqueness,
						request, requestartifact);

				coordinateResults.put(acoords, new DownloadFailedStructuredTaskResult("Failed to download " + acoords,
						ImmutableUtils.makeImmutableList(exceptions)));
				continue;
			}
			if ((file = resultartifact.getFile()) == null) {
				installLocalizationFailedDependencies(taskcontext, repositorybasedir, localrepomanager, cduniqueness,
						request, resultartifact);

				coordinateResults.put(acoords, new DownloadFailedStructuredTaskResult("Failed to download " + acoords,
						ImmutableUtils.makeImmutableList(exceptions)));
				continue;
			}
			SakerPath artifactpath = SakerPath.valueOf(file.getAbsolutePath());

			ContentDescriptor artifactcd = taskcontext.getTaskUtilities().getReportExecutionDependency(
					new ArtifactContentDescriptorExecutionProperty(cduniqueness, artifactpath));
			if (artifactcd == null) {
				coordinateResults.put(acoords, new DownloadFailedStructuredTaskResult("Failed to download " + acoords,
						ImmutableUtils.singletonList(
								new FileNotFoundException("Failed to retrieve content descriptor: " + artifactpath))));
				continue;
			}

			coordinateResults.put(acoords, new CompletedStructuredTaskResult(
					new ArtifactLocalizationWorkerTaskOutputImpl(acoords, artifactpath, artifactcd)));
		}
	}

	private static void installLocalizationFailedDependencies(TaskContext taskcontext, SakerPath repositorybasedir,
			LocalRepositoryManager localrepomanager, UUID cduniqueness, ArtifactRequest request, Artifact artifact) {
		SakerPath localartifactpath = repositorybasedir
				.resolve(SakerPath.valueOf(localrepomanager.getPathForLocalArtifact(artifact)));
		taskcontext.reportExecutionDependency(
				new ArtifactContentDescriptorExecutionProperty(cduniqueness, localartifactpath), null);

		for (RemoteRepository remoterepo : request.getRepositories()) {
			SakerPath artifactpath = repositorybasedir
					.resolve(SakerPath.valueOf(localrepomanager.getPathForRemoteArtifact(artifact, remoterepo, null)));
			taskcontext.reportExecutionDependency(
					new ArtifactContentDescriptorExecutionProperty(cduniqueness, artifactpath), null);
		}
	}

	@Override
	public Task<? extends ArtifactLocalizationTaskOutput> createTask(ExecutionContext executioncontext) {
		return this;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		SerialUtils.writeExternalCollection(out, artifacts);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		artifacts = SerialUtils.readExternalImmutableLinkedHashSet(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((artifacts == null) ? 0 : artifacts.hashCode());
		result = prime * result + ((configuration == null) ? 0 : configuration.hashCode());
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
		LocalizeArtifactsWorkerTaskFactory other = (LocalizeArtifactsWorkerTaskFactory) obj;
		if (artifacts == null) {
			if (other.artifacts != null)
				return false;
		} else if (!artifacts.equals(other.artifacts))
			return false;
		if (configuration == null) {
			if (other.configuration != null)
				return false;
		} else if (!configuration.equals(other.configuration))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (artifacts != null ? "artifacts=" + artifacts : "") + "]";
	}

}
