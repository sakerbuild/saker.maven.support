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
import saker.build.task.identifier.TaskIdentifier;
import saker.build.task.utils.SimpleStructuredObjectTaskResult;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.maven.support.api.ArtifactCoordinates;
import saker.maven.support.api.MavenOperationConfiguration;
import saker.maven.support.impl.install.ArtifactInstallWorkerTaskFactory;
import saker.maven.support.main.configuration.option.MavenConfigurationTaskOption;
import saker.nest.utils.FrontendTaskFactory;

//TODO taskdoc
public class InstallArtifactsTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Object>() {

			@SakerInput(value = { "ArtifactPath" }, required = true)
			public SakerPath artifactPathOption;
			@SakerInput(value = { "Coordinates" }, required = true)
			public ArtifactCoordinates coordinatesOption;

			@SakerInput(value = { "Configuration" })
			public MavenConfigurationTaskOption configurationOption;

			@Override
			public Object run(TaskContext taskcontext) throws Exception {
				if (artifactPathOption == null) {
					taskcontext.abortExecution(new IllegalArgumentException("ArtifactPath is null."));
					return null;
				}
				if (coordinatesOption == null) {
					taskcontext.abortExecution(new IllegalArgumentException("Coordinates is null."));
					return null;
				}
				MavenOperationConfiguration configuration = configurationOption.createConfiguration();
				ArtifactInstallWorkerTaskFactory task = new ArtifactInstallWorkerTaskFactory(configuration,
						coordinatesOption, artifactPathOption);
				TaskIdentifier taskid = task.createTaskIdentifier();
				taskcontext.startTask(taskid, task, null);

				SimpleStructuredObjectTaskResult result = new SimpleStructuredObjectTaskResult(taskid);
				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
				return result;
			}
		};
	}

}
