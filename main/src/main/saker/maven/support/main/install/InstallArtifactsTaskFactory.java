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
import saker.maven.support.main.TaskDocs;
import saker.maven.support.main.TaskDocs.DocArtifactInstallWorkerTaskOutput;
import saker.maven.support.main.TaskDocs.DocInputArtifactCoordinates;
import saker.maven.support.main.TaskDocs.DocInstallArtifactPath;
import saker.maven.support.main.configuration.option.MavenConfigurationTaskOption;
import saker.maven.support.main.configuration.option.MavenOperationConfigurationTaskOptionUtils;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.utils.FrontendTaskFactory;

@NestTaskInformation(returnType = @NestTypeUsage(DocArtifactInstallWorkerTaskOutput.class))
@NestInformation("Installs the specified file with the given coordinates to the local Maven repository.\n"
		+ "The task will simply install the file as an aritfact to the local repository.\n"
		+ "The local repository path can be configured as other saker.maven tasks. Remote repository "
		+ "configurations are ignored.")

@NestParameterInformation(value = "ArtifactPath",
		required = true,
		type = @NestTypeUsage(DocInstallArtifactPath.class),
		info = @NestInformation("Path to the artifact that should be installed to the repository."))
@NestParameterInformation(value = "Coordinates",
		required = true,
		type = @NestTypeUsage(DocInputArtifactCoordinates.class),
		info = @NestInformation("The coordinates of the installed artifact.\n"
				+ "The given file will be installed to the repository with the artifact coordinates specified "
				+ "in this parameter."))
@NestParameterInformation(value = "Configuration",
		type = @NestTypeUsage(MavenConfigurationTaskOption.class),
		info = @NestInformation(TaskDocs.PARAM_CONFIGURATION))
public class InstallArtifactsTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "saker.maven.install";

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
				MavenOperationConfiguration configuration = MavenOperationConfigurationTaskOptionUtils
						.createConfiguration(taskcontext, configurationOption);
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
