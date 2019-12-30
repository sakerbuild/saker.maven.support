package saker.maven.support.impl;

import java.util.Map;
import java.util.Set;

import saker.build.file.path.SakerPath;
import saker.build.task.TaskFactory;
import saker.build.task.identifier.TaskIdentifier;
import saker.maven.support.api.ArtifactCoordinates;
import saker.maven.support.api.MavenOperationConfiguration;
import saker.maven.support.api.dependency.MavenDependencyResolutionTaskOutput;
import saker.maven.support.api.download.ArtifactDownloadTaskOutput;
import saker.maven.support.api.download.ArtifactDownloadWorkerTaskOutput;
import saker.maven.support.api.localize.ArtifactLocalizationTaskOutput;
import saker.maven.support.impl.dependency.ResolveMavenArtifactDependencyWorkerTaskFactory;
import saker.maven.support.impl.dependency.ResolveMavenPomDependencyWorkerTaskFactory;
import saker.maven.support.impl.dependency.option.MavenDependencyOption;
import saker.maven.support.impl.download.ArtifactDownloadTaskIdentifierImpl;
import saker.maven.support.impl.download.ArtifactDownloadWorkerTaskFactory;
import saker.maven.support.impl.download.DownloadArtifactsWorkerTaskFactory;
import saker.maven.support.impl.localize.LocalizeArtifactsWorkerTaskFactory;

public class MavenSupportImpl {
	private MavenSupportImpl() {
		throw new UnsupportedOperationException();
	}

	public static TaskFactory<? extends ArtifactDownloadWorkerTaskOutput> createArtifactDownloadTaskFactory(
			String workinglocation, SakerPath repositorybasedir, SakerPath artifactrelpath,
			ArtifactCoordinates coordinates) {
		return new ArtifactDownloadWorkerTaskFactory(workinglocation, repositorybasedir, artifactrelpath, coordinates);
	}

	public static TaskIdentifier createArtifactDownloadTaskIdentifier(String workinglocation, SakerPath relpath) {
		return new ArtifactDownloadTaskIdentifierImpl(workinglocation, relpath);
	}

	public static TaskIdentifier createResolveMavenArtifactDependencyTaskIdentifier(MavenOperationConfiguration config,
			Map<? extends ArtifactCoordinates, ? extends MavenDependencyOption> coordinates) {
		if (config == null) {
			config = MavenOperationConfiguration.defaults();
		}
		return new ResolveMavenArtifactDependencyWorkerTaskFactory(coordinates, config);
	}

	public static TaskFactory<? extends MavenDependencyResolutionTaskOutput> createResolveMavenArtifactDependencyTaskFactory(
			MavenOperationConfiguration config,
			Map<? extends ArtifactCoordinates, ? extends MavenDependencyOption> coordinates) {
		if (config == null) {
			config = MavenOperationConfiguration.defaults();
		}
		return new ResolveMavenArtifactDependencyWorkerTaskFactory(coordinates, config);
	}

	public static TaskIdentifier createResolveMavenPomDependencyTaskIdentifier(MavenOperationConfiguration config,
			SakerPath pompath) {
		if (config == null) {
			config = MavenOperationConfiguration.defaults();
		}
		return new ResolveMavenPomDependencyWorkerTaskFactory(config, pompath);
	}

	public static TaskFactory<? extends MavenDependencyResolutionTaskOutput> createResolveMavenPomDependencyTaskFactory(
			MavenOperationConfiguration config, SakerPath pompath) {
		if (config == null) {
			config = MavenOperationConfiguration.defaults();
		}
		return new ResolveMavenPomDependencyWorkerTaskFactory(config, pompath);
	}

	public static TaskFactory<? extends ArtifactDownloadTaskOutput> createDownloadArtifactsTaskFactory(
			MavenOperationConfiguration config, Set<? extends ArtifactCoordinates> coordinates) {
		return new DownloadArtifactsWorkerTaskFactory(config, coordinates);
	}

	public static TaskIdentifier createDownloadArtifactsTaskIdentifier(MavenOperationConfiguration config,
			Set<? extends ArtifactCoordinates> coordinates) {
		return new DownloadArtifactsWorkerTaskFactory(config, coordinates);
	}

	public static TaskFactory<? extends ArtifactLocalizationTaskOutput> createLocalizeArtifactsTaskFactory(
			MavenOperationConfiguration config, Set<? extends ArtifactCoordinates> coordinates) {
		return new LocalizeArtifactsWorkerTaskFactory(config, coordinates);
	}

	public static TaskIdentifier createLocalizeArtifactsTaskIdentifier(MavenOperationConfiguration config,
			Set<? extends ArtifactCoordinates> coordinates) {
		return new LocalizeArtifactsWorkerTaskFactory(config, coordinates);
	}
}
