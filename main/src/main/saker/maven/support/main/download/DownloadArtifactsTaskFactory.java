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
package saker.maven.support.main.download;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.task.identifier.TaskIdentifier;
import saker.build.task.utils.SimpleStructuredObjectTaskResult;
import saker.build.task.utils.StructuredListTaskResult;
import saker.build.task.utils.StructuredTaskResult;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.maven.support.api.ArtifactCoordinates;
import saker.maven.support.api.MavenOperationConfiguration;
import saker.maven.support.api.dependency.MavenDependencyResolutionTaskOutput;
import saker.maven.support.api.dependency.ResolvedDependencyArtifact;
import saker.maven.support.api.download.ArtifactDownloadTaskOutput;
import saker.maven.support.impl.MavenSupportImpl;
import saker.maven.support.main.TaskDocs;
import saker.maven.support.main.TaskDocs.DocArtifactDownloadTaskOutput;
import saker.maven.support.main.TaskDocs.DocInputArtifactCoordinates;
import saker.maven.support.main.configuration.option.MavenConfigurationTaskOption;
import saker.maven.support.main.configuration.option.MavenOperationConfigurationTaskOptionUtils;
import saker.maven.support.main.dependency.ResolveMavenDependencyTaskFactory;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.utils.FrontendTaskFactory;

@NestTaskInformation(returnType = @NestTypeUsage(DocArtifactDownloadTaskOutput.class))
@NestInformation("Downloads the specified artifacts for the build execution.\n"
		+ "The task will put the specified artifact files under the build directory and makes them available to the build execution.\n"
		+ "Artifact \"downloading\" doesn't necessarily involve network communication. Downloading is merely retrieving the artifacts "
		+ "from the Maven repository or its cache.\n"
		+ "This task doesn't perform any dependency resolution. To resolve dependencies, use the "
		+ ResolveMavenDependencyTaskFactory.TASK_NAME + "() task.")
@NestParameterInformation(value = "Artifacts",
		aliases = { "", "Artifact" },
		type = @NestTypeUsage(value = Collection.class, elementTypes = DocInputArtifactCoordinates.class),
		info = @NestInformation("Specifies one or more artifact coordinates to be downloaded."
				+ "The artifact coordinates are expected in the <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version> format.\n"
				+ "The dependencies of the artifacts are NOT resolved.\n" + "This parameter accepts the output of the "
				+ ResolveMavenDependencyTaskFactory.TASK_NAME + "() task to download the resolved artifacts."))
@NestParameterInformation(value = "Configuration",
		type = @NestTypeUsage(MavenConfigurationTaskOption.class),
		info = @NestInformation(TaskDocs.PARAM_CONFIGURATION))
public class DownloadArtifactsTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "saker.maven.download";

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Object>() {
			@SakerInput(value = { "", "Artifact", "Artifacts" }, required = true)
			public Object artifacts;

			@SakerInput(value = { "Configuration" })
			public MavenConfigurationTaskOption configuration;

			@Override
			public Object run(TaskContext taskcontext) throws Exception {
				if (artifacts instanceof StructuredTaskResult) {
					if (artifacts instanceof StructuredListTaskResult) {
						StructuredListTaskResult arifactsstructuredlist = (StructuredListTaskResult) artifacts;
						Set<ArtifactCoordinates> coordinates = new LinkedHashSet<>();
						Iterator<? extends StructuredTaskResult> it = arifactsstructuredlist.resultIterator();
						while (it.hasNext()) {
							Object resobj = it.next().toResult(taskcontext);
							String resstr = Objects.toString(resobj, null);
							if (ObjectUtils.isNullOrEmpty(resstr)) {
								continue;
							}
							try {
								coordinates.add(ArtifactCoordinates.valueOf(resstr));
							} catch (IllegalArgumentException e) {
								taskcontext.abortExecution(e);
								return null;
							}
						}
						return handleArtifactCoordinates(taskcontext, getRepositoryOperationConfiguration(),
								coordinates);
					}
					StructuredTaskResult structuredartifacts = (StructuredTaskResult) artifacts;
					artifacts = structuredartifacts.toResult(taskcontext);
				}
				if (artifacts instanceof Object[]) {
					artifacts = ImmutableUtils.makeImmutableList((Object[]) artifacts);
				}
				if (artifacts instanceof Iterable<?>) {
					Iterable<?> artifactsiterable = (Iterable<?>) artifacts;
					Set<ArtifactCoordinates> coordinates = new LinkedHashSet<>();

					for (Object o : artifactsiterable) {
						String coordstr = Objects.toString(o, null);
						if (ObjectUtils.isNullOrEmpty(coordstr)) {
							continue;
						}
						try {
							coordinates.add(ArtifactCoordinates.valueOf(coordstr));
						} catch (IllegalArgumentException e) {
							taskcontext.abortExecution(e);
							return null;
						}
					}
					return handleArtifactCoordinates(taskcontext, getRepositoryOperationConfiguration(), coordinates);
				}

				//TODO handle artifact localization result?

				if (artifacts instanceof MavenDependencyResolutionTaskOutput) {
					MavenDependencyResolutionTaskOutput depoutput = (MavenDependencyResolutionTaskOutput) artifacts;
					Set<ArtifactCoordinates> coordinates = ImmutableUtils
							.makeImmutableLinkedHashSet(depoutput.getArtifactCoordinates());
					return handleArtifactCoordinates(taskcontext, depoutput.getConfiguration(), coordinates);
				}
				if (artifacts instanceof ResolvedDependencyArtifact) {
					ResolvedDependencyArtifact resolvedartifact = (ResolvedDependencyArtifact) artifacts;
					return handleArtifactCoordinates(taskcontext, resolvedartifact.getConfiguration(),
							ImmutableUtils.singletonSet(resolvedartifact.getCoordinates()));
				}

				String coordsstr = Objects.toString(artifacts, null);
				if (coordsstr == null) {
					NullPointerException npe = new NullPointerException("null Artifacts input argument.");
					taskcontext.abortExecution(npe);
					return null;
				}
				try {
					return handleArtifactCoordinates(taskcontext, getRepositoryOperationConfiguration(),
							Collections.singleton(ArtifactCoordinates.valueOf(coordsstr)));
				} catch (IllegalArgumentException e) {
					taskcontext.abortExecution(e);
					return null;
				}
			}

			private MavenOperationConfiguration getRepositoryOperationConfiguration() {
				MavenOperationConfiguration config = MavenOperationConfigurationTaskOptionUtils
						.createConfiguration(this.configuration);
				return config;
			}
		};
	}

	private static Object handleArtifactCoordinates(TaskContext taskcontext, MavenOperationConfiguration config,
			Set<ArtifactCoordinates> coordinates) {
		TaskFactory<? extends ArtifactDownloadTaskOutput> dltaskfactory = MavenSupportImpl
				.createDownloadArtifactsTaskFactory(config, coordinates);
		TaskIdentifier dltaskid = MavenSupportImpl.createDownloadArtifactsTaskIdentifier(config, coordinates);

		taskcontext.startTask(dltaskid, dltaskfactory, null);
		SimpleStructuredObjectTaskResult result = new SimpleStructuredObjectTaskResult(dltaskid);
		taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
		return result;
	}

}
