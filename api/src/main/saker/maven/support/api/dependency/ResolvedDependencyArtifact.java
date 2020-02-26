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
package saker.maven.support.api.dependency;

import saker.maven.support.api.ArtifactCoordinates;
import saker.maven.support.api.MavenOperationConfiguration;

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

	/**
	 * Gets the {@link MavenOperationConfiguration} that was used when performing the dependency resolution.
	 * 
	 * @return The configuration.
	 */
	public MavenOperationConfiguration getConfiguration();
}
