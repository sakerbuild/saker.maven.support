package saker.maven.support.api.download;

import saker.build.file.content.ContentDescriptor;
import saker.build.file.path.SakerPath;
import saker.maven.support.api.ArtifactCoordinates;

/**
 * Represents the result of a single Maven artifact download task.
 * <p>
 * The interface provides access to various information about the downloaded artifact.
 * <p>
 * Clients shouldn't implement this interface.
 */
public interface ArtifactDownloadWorkerTaskOutput {
	/**
	 * Gets the coordinates of the downloaded artifact.
	 * 
	 * @return The artifact coordinates.
	 */
	public ArtifactCoordinates getCoordinates();

	/**
	 * Gets the execution path of the downloaded artifact in the build file hierarchy.
	 * 
	 * @return The absolute execution path.
	 */
	public SakerPath getPath();

	/**
	 * Gets the content descriptor of the downloaded artifact.
	 * 
	 * @return The content descriptor.
	 */
	public ContentDescriptor getContentDescriptor();
}
