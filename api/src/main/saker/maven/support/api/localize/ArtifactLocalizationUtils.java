package saker.maven.support.api.localize;

import java.util.Set;

import saker.build.task.TaskFactory;
import saker.build.task.identifier.TaskIdentifier;
import saker.maven.support.api.ArtifactCoordinates;
import saker.maven.support.api.MavenOperationConfiguration;
import saker.maven.support.impl.localize.LocalizeArtifactsWorkerTaskFactory;

/**
 * Utility class with functions to interact with Maven artifact localization.
 */
public class ArtifactLocalizationUtils {
	private ArtifactLocalizationUtils() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Creates a new task that localizes the specified artifacts.
	 * 
	 * @param config
	 *            The configuration to use during the operation or <code>null</code> to use the
	 *            {@linkplain MavenOperationConfiguration#defaults() defaults}.
	 * @param coordinates
	 *            The artifact coordinates to localize.
	 * @return The localizing task factory.
	 * @throws NullPointerException
	 *             If <code>coordinates</code> is <code>null</code>.
	 * @see #createLocalizeArtifactsTaskIdentifier(MavenOperationConfiguration, Set)
	 */
	public static TaskFactory<? extends ArtifactLocalizationTaskOutput> createLocalizeArtifactsTaskFactory(
			MavenOperationConfiguration config, Set<? extends ArtifactCoordinates> coordinates) {
		return new LocalizeArtifactsWorkerTaskFactory(config, coordinates);
	}

	/**
	 * Creates a task identifier for the artifact localization task.
	 * <p>
	 * The created task identifier should be userd with the result of
	 * {@link #createLocalizeArtifactsTaskFactory(MavenOperationConfiguration, Set)}.
	 * 
	 * @param config
	 *            The configuration to use during the operation or <code>null</code> to use the
	 *            {@linkplain MavenOperationConfiguration#defaults() defaults}.
	 * @param coordinates
	 *            The artifact coordinates to localize.
	 * @return The task identifier.
	 * @throws NullPointerException
	 *             If <code>coordinates</code> is <code>null</code>.
	 * @see #createLocalizeArtifactsTaskFactory(MavenOperationConfiguration, Set)
	 */
	public static TaskIdentifier createLocalizeArtifactsTaskIdentifier(MavenOperationConfiguration config,
			Set<? extends ArtifactCoordinates> coordinates) {
		return new LocalizeArtifactsWorkerTaskFactory(config, coordinates);
	}
}
