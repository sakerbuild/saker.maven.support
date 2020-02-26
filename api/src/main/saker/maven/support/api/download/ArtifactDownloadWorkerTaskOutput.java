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

	//for conversion compatibility
	/**
	 * Gets the execution path of the downloaded artifact in the build file hierarchy.
	 * 
	 * @return The absolute execution path.
	 */
	public default SakerPath toSakerPath() {
		return getPath();
	}
}
