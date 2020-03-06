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
package saker.maven.support.main.dependency;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.task.identifier.TaskIdentifier;
import saker.build.task.utils.SimpleStructuredObjectTaskResult;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.maven.support.api.ArtifactCoordinates;
import saker.maven.support.api.MavenOperationConfiguration;
import saker.maven.support.api.dependency.MavenDependencyResolutionTaskOutput;
import saker.maven.support.impl.MavenSupportImpl;
import saker.maven.support.impl.dependency.option.ExclusionOption;
import saker.maven.support.impl.dependency.option.MavenDependencyOption;
import saker.maven.support.main.TaskDocs;
import saker.maven.support.main.TaskDocs.DocInputArtifactCoordinates;
import saker.maven.support.main.TaskDocs.DocMavenDependencyResolutionTaskOutput;
import saker.maven.support.main.TaskDocs.DocMavenPomPath;
import saker.maven.support.main.configuration.option.MavenConfigurationTaskOption;
import saker.maven.support.main.configuration.option.MavenOperationConfigurationTaskOptionUtils;
import saker.maven.support.main.dependency.option.ExclusionTaskOption;
import saker.maven.support.main.dependency.option.MavenDependencyTaskOption;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.utils.FrontendTaskFactory;

@NestTaskInformation(returnType = @NestTypeUsage(DocMavenDependencyResolutionTaskOutput.class))
@NestInformation("Resolves Maven dependencies using the specified configuration.\n"
		+ "The task can be used to resolve dependencies from Maven repositories using direct artifact names, "
		+ "pom.xml files, or dependency specifications.\n"
		+ "The task will transitively resolve the dependencies based on the arguments, and will return reference to the "
		+ "resolved artifacts. The callers are likely to pass it to another task that converts it to an usable input for "
		+ "other tasks. (E.g. classpath, downloading, etc...)\n"
		+ "This task may initiate network requests in order to complete its work.\n"
		+ "The task uses the Maven Resolver library to execute its operations. For exact mechanism of the dependency resolution, "
		+ "consult the documentation of the library at: https://maven.apache.org/resolver/index.html")
@NestParameterInformation(value = "Artifacts",
		aliases = { "", "Artifact" },
		type = @NestTypeUsage(value = Collection.class, elementTypes = DocInputArtifactCoordinates.class),
		info = @NestInformation("Specifies the artifact coordinates that should be resolved.\n"
				+ "The artifact coordinates are expected in the <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version> format.\n"
				+ "The coordinates are directly passed to the Maven resolver backend.\n"
				+ "Using this parameter, the scope of the dependencies will be "
				+ ResolveMavenDependencyTaskFactory.DEFAULT_DEPENDENCY_SCOPE + ".\n"
				+ "This parameter cannot be used together with Pom or Dependencies.\n"
				+ "If the <extension> part is omitted, it will be inferred from the <packaging> tag of the associated pom file."))
@NestParameterInformation(value = "Pom",
		type = @NestTypeUsage(DocMavenPomPath.class),
		info = @NestInformation("Specifies a path to a pom.xml file from which the dependencies should be resolved.\n"
				+ "The task will parse the specified pom.xml and resolve the dependencies of it. The specified file doesn't "
				+ "necessarily need to have the pom.xml name.\n"
				+ "This parameter cannot be used together with Artifacts or Dependencies."))
@NestParameterInformation(value = "Dependencies",
		type = @NestTypeUsage(value = Map.class,
				elementTypes = { DocInputArtifactCoordinates.class, MavenDependencyTaskOption.class }),
		info = @NestInformation("Specifies artifact dependencies that should be resolved.\n"
				+ "The dependencies are specified in a map with artifact coordinate keys, and dependency specification values.\n"
				+ "The dependency specifications contain the scope, exclusions and optionality of the dependency. If no scope "
				+ "is specified, " + ResolveMavenDependencyTaskFactory.DEFAULT_DEPENDENCY_SCOPE + " is used.\n"
				+ "This parameter cannot be used together with Artifacts or Pom.\n"
				+ "The parameter is set to work the same way as the <dependency/> element(s) in the pom.xml.\n"
				+ "If the extension part of the coordinates is omitted, it will be inferred from the <packaging> tag of the associated pom file."))
@NestParameterInformation(value = "Configuration",
		type = @NestTypeUsage(MavenConfigurationTaskOption.class),
		info = @NestInformation(TaskDocs.PARAM_CONFIGURATION))
public class ResolveMavenDependencyTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "saker.maven.resolve";

	static final String DEFAULT_DEPENDENCY_SCOPE = "compile";

	private static final MavenDependencyOption DEPENDENCYOPTION_SCOPE_COMPILE = new MavenDependencyOption(
			DEFAULT_DEPENDENCY_SCOPE, null, null);

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Object>() {

			@SakerInput(value = { "", "Artifact", "Artifacts" })
			public Optional<Collection<String>> artifacts;

			@SakerInput(value = { "Pom" })
			public Optional<SakerPath> pom;

			@SakerInput(value = { "Dependencies" })
			public Optional<Map<String, MavenDependencyTaskOption>> dependencies;

			@SakerInput(value = { "Configuration" })
			public MavenConfigurationTaskOption configuration;

			@Override
			public Object run(TaskContext taskcontext) throws Exception {
				if (Collections.frequency(Arrays.asList(artifacts, pom, dependencies), null) != 2) {
					taskcontext.abortExecution(new IllegalArgumentException(
							"Only one argument can be specified of: Artifacts, Dependencies, Pom."));
					return null;
				}
				MavenOperationConfiguration config = MavenOperationConfigurationTaskOptionUtils
						.createConfiguration(this.configuration);
				TaskIdentifier workertaskid;
				if (artifacts != null) {
					Collection<String> dependenciescollection = artifacts.get();
					if (ObjectUtils.isNullOrEmpty(dependenciescollection)) {
						taskcontext.abortExecution(new IllegalArgumentException("No artifacts specified."));
						return null;
					}
					Map<ArtifactCoordinates, MavenDependencyOption> coordinates = new LinkedHashMap<>();
					for (String depcoord : dependenciescollection) {
						if (ObjectUtils.isNullOrEmpty(depcoord)) {
							continue;
						}
						try {
							coordinates.put(ArtifactCoordinates.valueOf(depcoord), DEPENDENCYOPTION_SCOPE_COMPILE);
						} catch (IllegalArgumentException e) {
							taskcontext.abortExecution(e);
							return null;
						}
					}
					if (ObjectUtils.isNullOrEmpty(coordinates)) {
						taskcontext.abortExecution(new IllegalArgumentException("No artifacts specified."));
						return null;
					}
					TaskFactory<? extends MavenDependencyResolutionTaskOutput> task = MavenSupportImpl
							.createResolveMavenArtifactDependencyTaskFactory(config, coordinates);
					workertaskid = MavenSupportImpl.createResolveMavenArtifactDependencyTaskIdentifier(config,
							coordinates);
					taskcontext.startTask(workertaskid, task, null);
				} else if (pom != null) {
					SakerPath pompath = pom.get();

					if (pompath == null) {
						taskcontext.abortExecution(new IllegalArgumentException("Pom path is null."));
						return null;
					}

					TaskFactory<? extends MavenDependencyResolutionTaskOutput> task = MavenSupportImpl
							.createResolveMavenPomDependencyTaskFactory(config, pompath);
					workertaskid = MavenSupportImpl.createResolveMavenPomDependencyTaskIdentifier(config, pompath);
					taskcontext.startTask(workertaskid, task, null);
				} else if (dependencies != null) {
					Map<ArtifactCoordinates, MavenDependencyOption> coordinates = new LinkedHashMap<>();
					Map<String, MavenDependencyTaskOption> paramdepmap = this.dependencies.get();
					if (ObjectUtils.isNullOrEmpty(paramdepmap)) {
						taskcontext.abortExecution(new IllegalArgumentException("No dependencies specified."));
						return null;
					}
					for (Entry<String, MavenDependencyTaskOption> entry : paramdepmap.entrySet()) {
						ArtifactCoordinates acoords;
						try {
							acoords = ArtifactCoordinates.valueOf(entry.getKey());
						} catch (IllegalArgumentException e) {
							taskcontext.abortExecution(e);
							return null;
						}
						MavenDependencyTaskOption entrydepoption = entry.getValue();
						MavenDependencyOption coorddepoption;
						if (entrydepoption == null) {
							coorddepoption = DEPENDENCYOPTION_SCOPE_COMPILE;
						} else {
							Set<ExclusionOption> exclusions;
							Collection<ExclusionTaskOption> excltaskoptions = entrydepoption.getExclusions();
							if (excltaskoptions == null) {
								exclusions = null;
							} else {
								exclusions = new LinkedHashSet<>();
								for (ExclusionTaskOption excltaskoption : excltaskoptions) {
									if (excltaskoption == null) {
										continue;
									}
									exclusions.add(new ExclusionOption(excltaskoption.getGroupId(),
											excltaskoption.getArtifactId(), excltaskoption.getClassifier(),
											excltaskoption.getExtension()));
								}
							}
							coorddepoption = new MavenDependencyOption(
									ObjectUtils.nullDefault(entrydepoption.getScope(), DEFAULT_DEPENDENCY_SCOPE),
									entrydepoption.getOptional(), exclusions);
						}
						coordinates.put(acoords, coorddepoption);
					}
					TaskFactory<? extends MavenDependencyResolutionTaskOutput> task = MavenSupportImpl
							.createResolveMavenArtifactDependencyTaskFactory(config, coordinates);
					workertaskid = MavenSupportImpl.createResolveMavenArtifactDependencyTaskIdentifier(config,
							coordinates);
					taskcontext.startTask(workertaskid, task, null);
				} else {
					throw new AssertionError("Internal error: unreachable");
				}

				SimpleStructuredObjectTaskResult result = new SimpleStructuredObjectTaskResult(workertaskid);
				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
				return result;
			}
		};
	}

}
