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
package saker.maven.support.main.install;

import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.utils.annot.SakerInput;
import saker.maven.support.api.ArtifactCoordinates;
import saker.maven.support.impl.install.ArtifactInstallWorkerTaskFactory;
import saker.maven.support.main.configuration.option.MavenConfigurationTaskOption;
import saker.nest.utils.FrontendTaskFactory;

//TODO taskdoc
public class InstallArtifactsTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		// TODO Auto-generated method stub
		return new ParameterizableTask<Object>() {

			@SakerInput("ArtifactPath")
			public SakerPath artifactPath;
			@SakerInput("Coordinates")
			public ArtifactCoordinates coordinates;

			@SakerInput(value = { "Configuration" })
			public MavenConfigurationTaskOption configuration;

			@Override
			public Object run(TaskContext taskcontext) throws Exception {
				// TODO Auto-generated method stub
				ArtifactInstallWorkerTaskFactory task = new ArtifactInstallWorkerTaskFactory(
						configuration.createConfiguration(), artifactPath, coordinates);
				taskcontext.startTask(task, task, null);
				return null;
			}
		};
	}

}
