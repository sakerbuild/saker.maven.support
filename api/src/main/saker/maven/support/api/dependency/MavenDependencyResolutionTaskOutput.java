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

import java.util.Collection;

import saker.maven.support.api.ArtifactCoordinates;
import saker.maven.support.api.MavenOperationConfiguration;

/**
 * Provides access to the results of the dependency resolution task.
 * <p>
 * The result of the resolution is a set of artifact coordinates paired with the dependency scope they were resolved
 * with.
 * <p>
 * Clients shouldn't implement this interface.
 */
public interface MavenDependencyResolutionTaskOutput {
	/**
	 * Gets the {@link MavenOperationConfiguration} that was used when performing the dependency resolution.
	 * 
	 * @return The configuration.
	 */
	public MavenOperationConfiguration getConfiguration();

	/**
	 * Gets the artifact coordinates that were resolved during the dependency resolution.
	 * 
	 * @return The coordinates of the resolved artifacts.
	 */
	public Collection<ArtifactCoordinates> getArtifactCoordinates();

	/**
	 * Gets the artifact resolutions that are the result of the operation.
	 * <p>
	 * The elements provide access to the artifact coordinates and the scope of the associated dependency.
	 * 
	 * @return The resolved artifacts.
	 */
	public Collection<ResolvedDependencyArtifact> getResolvedArtifacts();

	/**
	 * Gets an object that can be used to limit the output by the specified dependency scopes.
	 * <p>
	 * The result object provides access to the same artifacts as this, but can filter out unnecessary dependency
	 * scopes.
	 * 
	 * @return The scope output.
	 */
	public MavenDependencyResolutionScopesOutput getScopes();
}
