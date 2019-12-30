package saker.maven.support.api.download;

import java.util.Set;

import saker.build.task.TaskFactory;
import saker.build.task.identifier.TaskIdentifier;
import saker.maven.support.api.ArtifactCoordinates;
import saker.maven.support.api.MavenOperationConfiguration;
import saker.maven.support.impl.MavenSupportImpl;

/**
 * Utility class with functions to interact with Maven artifact downloading.
 */
public class ArtifactDownloadUtils {
	private ArtifactDownloadUtils() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Creates a new task that downloads the specified artifacts.
	 * 
	 * @param config
	 *            The configuration to use during the operation or <code>null</code> to use the
	 *            {@linkplain MavenOperationConfiguration#defaults() defaults}.
	 * @param coordinates
	 *            The artifact coordinates to download.
	 * @return The downloading task factory.
	 * @throws NullPointerException
	 *             If <code>coordinates</code> is <code>null</code>.
	 * @see #createDownloadArtifactsTaskIdentifier(MavenOperationConfiguration, Set)
	 */
	public static TaskFactory<? extends ArtifactDownloadTaskOutput> createDownloadArtifactsTaskFactory(
			MavenOperationConfiguration config, Set<? extends ArtifactCoordinates> coordinates)
			throws NullPointerException {
		return MavenSupportImpl.createDownloadArtifactsTaskFactory(config, coordinates);
	}

	/**
	 * Creates a task identifier for the artifact downloading task.
	 * <p>
	 * The created task identifier should be userd with the result of
	 * {@link #createDownloadArtifactsTaskFactory(MavenOperationConfiguration, Set)}.
	 * 
	 * @param config
	 *            The configuration to use during the operation or <code>null</code> to use the
	 *            {@linkplain MavenOperationConfiguration#defaults() defaults}.
	 * @param coordinates
	 *            The artifact coordinates to download.
	 * @return The task identifier.
	 * @throws NullPointerException
	 *             If <code>coordinates</code> is <code>null</code>.
	 * @see #createDownloadArtifactsTaskFactory(MavenOperationConfiguration, Set)
	 */
	public static TaskIdentifier createDownloadArtifactsTaskIdentifier(MavenOperationConfiguration config,
			Set<? extends ArtifactCoordinates> coordinates) throws NullPointerException {
		return MavenSupportImpl.createDownloadArtifactsTaskIdentifier(config, coordinates);
	}
}
