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

import java.util.Set;

import saker.build.task.TaskFactory;
import saker.build.task.identifier.TaskIdentifier;
import saker.maven.support.api.ArtifactCoordinates;
import saker.maven.support.api.MavenOperationConfiguration;
import saker.maven.support.impl.localize.LocalizeArtifactsWorkerTaskFactory;

/**
 * Utility class with functions to interact with Maven artifact localization.
 */
public class ArtifactLocalizationUtils {
	private ArtifactLocalizationUtils() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Creates a new task that localizes the specified artifacts.
	 * 
	 * @param config
	 *            The configuration to use during the operation or <code>null</code> to use the
	 *            {@linkplain MavenOperationConfiguration#defaults() defaults}.
	 * @param coordinates
	 *            The artifact coordinates to localize.
	 * @return The localizing task factory.
	 * @throws NullPointerException
	 *             If <code>coordinates</code> is <code>null</code>.
	 * @see #createLocalizeArtifactsTaskIdentifier(MavenOperationConfiguration, Set)
	 */
	public static TaskFactory<? extends ArtifactLocalizationTaskOutput> createLocalizeArtifactsTaskFactory(
			MavenOperationConfiguration config, Set<? extends ArtifactCoordinates> coordinates) {
		return new LocalizeArtifactsWorkerTaskFactory(config, coordinates);
	}

	/**
	 * Creates a task identifier for the artifact localization task.
	 * <p>
	 * The created task identifier should be userd with the result of
	 * {@link #createLocalizeArtifactsTaskFactory(MavenOperationConfiguration, Set)}.
	 * 
	 * @param config
	 *            The configuration to use during the operation or <code>null</code> to use the
	 *            {@linkplain MavenOperationConfiguration#defaults() defaults}.
	 * @param coordinates
	 *            The artifact coordinates to localize.
	 * @return The task identifier.
	 * @throws NullPointerException
	 *             If <code>coordinates</code> is <code>null</code>.
	 * @see #createLocalizeArtifactsTaskFactory(MavenOperationConfiguration, Set)
	 */
	public static TaskIdentifier createLocalizeArtifactsTaskIdentifier(MavenOperationConfiguration config,
			Set<? extends ArtifactCoordinates> coordinates) {
		return new LocalizeArtifactsWorkerTaskFactory(config, coordinates);
	}
}
