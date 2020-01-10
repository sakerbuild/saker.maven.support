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
package saker.maven.support.api.install;

import saker.build.file.path.SakerPath;
import saker.maven.support.api.ArtifactCoordinates;

/**
 * Output of an artifact installation worker task.
 * <p>
 * The interface provides access to the resulting local path of the artifact as well as the coordinates of it.
 * <p>
 * Clients shouldn't implement this interface.
 */
public interface ArtifactInstallWorkerTaskOutput {
	/**
	 * Gets the local path to the installed artifact.
	 * 
	 * @return The absolute local file system path.
	 */
	public SakerPath getArtifactLocalPath();

	/**
	 * Gets the coordinates of the installed artifact.
	 * 
	 * @return The coordinates.
	 */
	public ArtifactCoordinates getCoordinates();
}
