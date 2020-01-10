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
package saker.maven.support.api.deploy;

import saker.maven.support.api.ArtifactCoordinates;

/**
 * Output of an artifact deployment worker task.
 * <p>
 * The interface provides access to the coordinates of the deployed artifacts.
 * <p>
 * Currently the interface doesn't expose other information about the deployed artifacts.
 * <p>
 * Clients shouldn't implement this interface.
 */
public interface ArtifactDeployWorkerTaskOutput {
	/**
	 * Gets the deployment coordinates.
	 * <p>
	 * The classifier and extension of the result is <code>null</code>.
	 * 
	 * @return The coordinates.
	 */
	public ArtifactCoordinates getCoordinates();
}
