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
package saker.maven.support.api.localize;

import java.util.Collection;

import saker.build.file.path.SakerPath;
import saker.build.task.utils.StructuredListTaskResult;
import saker.build.task.utils.StructuredTaskResult;
import saker.maven.support.api.ArtifactCoordinates;
import saker.maven.support.api.MavenOperationConfiguration;

/**
 * Provides access to the output of a Maven artifact localization task.
 * <p>
 * The interface allows accessing the results of the localization. The actual result are accessible using structured
 * task result, as the localization of artifacts are separated into multiple worker tasks.
 * <p>
 * Clients shouldn't implement this interface.
 */
public interface ArtifactLocalizationTaskOutput {
	/**
	 * Gets the {@link MavenOperationConfiguration} that was used when performing the artifact localization.
	 * 
	 * @return The configuration.
	 */
	public MavenOperationConfiguration getConfiguration();

	//element SakerPath
	/**
	 * Gets the localized artifact absolute local file system paths.
	 * <p>
	 * Each element in the result is an instance of {@link SakerPath}.
	 * 
	 * @return The structured task result for the artifact paths.
	 */
	public StructuredListTaskResult getArtifactLocalPaths();

	//element ArtifactLocalizationWorkerTaskOutput
	/**
	 * Gets the localized artifact worker task results.
	 * <p>
	 * Each element in the result is an instance of {@link ArtifactLocalizationWorkerTaskOutput}.
	 * 
	 * @return The structured task result for the artifact localization worker tasks.
	 */
	public StructuredListTaskResult getLocalizationResults();

	//ArtifactLocalizationWorkerTaskOutput
	/**
	 * Gets the localization task result for the given artifact coordinates.
	 * <p>
	 * The task result is an instance of {@link ArtifactLocalizationWorkerTaskOutput}.
	 * 
	 * @param artifactcoordinates
	 *            The artifact coordinates.
	 * @return The result of the localization of the specified artifact or <code>null</code> if not found.
	 */
	public StructuredTaskResult getLocalizationResult(ArtifactCoordinates artifactcoordinates);

	/**
	 * Gets the artifact coordinates that were localized by this task.
	 * 
	 * @return The artifact coordinates.
	 */
	public Collection<ArtifactCoordinates> getCoordinates();
}
