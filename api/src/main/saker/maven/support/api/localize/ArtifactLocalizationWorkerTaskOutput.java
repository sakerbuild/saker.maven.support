package saker.maven.support.api.localize;

import saker.build.file.content.ContentDescriptor;
import saker.build.file.path.SakerPath;
import saker.maven.support.api.ArtifactCoordinates;

/**
 * Represents the result of a single Maven artifact localization task.
 * <p>
 * The interface provides access to various information about the localized artifact.
 * <p>
 * Clients shouldn't implement this interface.
 */
public interface ArtifactLocalizationWorkerTaskOutput {
	/**
	 * Gets the coordinates of the localized artifact.
	 * 
	 * @return The artifact coordinates.
	 */
	public ArtifactCoordinates getCoordinates();

	/**
	 * Gets the absolute local file system path of the localized artifact.
	 * 
	 * @return The path.
	 */
	public SakerPath getLocalPath();

	/**
	 * Gets the content descriptor of the localized artifact.
	 * 
	 * @return The content descriptor.
	 */
	public ContentDescriptor getContentDescriptor();
}
