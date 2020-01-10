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
package saker.maven.support.main.deploy;

import java.util.Map;
import java.util.NavigableMap;

import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.identifier.TaskIdentifier;
import saker.build.task.utils.SimpleStructuredObjectTaskResult;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.maven.support.api.ArtifactCoordinates;
import saker.maven.support.impl.deploy.ArtifactDeployWorkerTaskFactory;
import saker.maven.support.main.configuration.option.MavenOperationConfigurationTaskOptionUtils;
import saker.maven.support.main.configuration.option.RepositoryTaskOption;
import saker.nest.utils.FrontendTaskFactory;

//TODO taskdoc
public class DeployArtifactsTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "saker.maven.deploy";

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Object>() {
			@SakerInput(value = { "", "Artifacts" }, required = true)
			public Map<String, SakerPath> artifactsOption;
			@SakerInput(value = { "Coordinates" }, required = true)
			public ArtifactCoordinates coordinatesOption;

			@SakerInput(value = { "RemoteRepository" }, required = true)
			public RepositoryTaskOption repositoryOption;

			@Override
			public Object run(TaskContext taskcontext) throws Exception {
				if (artifactsOption == null) {
					taskcontext.abortExecution(new IllegalArgumentException("Artifacts is null."));
					return null;
				}
				if (coordinatesOption == null) {
					taskcontext.abortExecution(new IllegalArgumentException("Coordinates is null."));
					return null;
				}
				NavigableMap<String, SakerPath> artifacts = ImmutableUtils.makeImmutableNavigableMap(artifactsOption);

				ArtifactCoordinates coordinates = new ArtifactCoordinates(coordinatesOption.getGroupId(),
						coordinatesOption.getArtifactId(), null, null, coordinatesOption.getVersion());

				ArtifactDeployWorkerTaskFactory task = new ArtifactDeployWorkerTaskFactory(
						MavenOperationConfigurationTaskOptionUtils.createRepositoryConfiguration(repositoryOption),
						coordinates, artifacts);
				TaskIdentifier taskid = task.createTaskIdentifier();
				taskcontext.startTask(taskid, task, null);
				SimpleStructuredObjectTaskResult result = new SimpleStructuredObjectTaskResult(taskid);
				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
				return result;
			}
		};
	}

}
