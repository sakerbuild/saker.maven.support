package saker.maven.support.api.dependency;

import saker.maven.support.api.ArtifactCoordinates;

/**
 * Represents a single artifact that was resolved for a given dependency.
 * <p>
 * The interface encloses the {@linkplain #getCoordinates() coordinates} of the artifact and the {@linkplain #getScope()
 * scope} of the associated dependency.
 * <p>
 * Clients shouldn't implement this interface.
 */
public interface ResolvedDependencyArtifact {
	/**
	 * Gets the artifact coordinates.
	 * 
	 * @return The coordinates.
	 */
	public ArtifactCoordinates getCoordinates();

	/**
	 * Gets the scope of the dependency.
	 * 
	 * @return The dependency scope.
	 */
	public String getScope();
}
