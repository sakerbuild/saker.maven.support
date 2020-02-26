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
package saker.maven.support.main;

import java.util.Collection;

import saker.build.file.path.SakerPath;
import saker.build.scripting.model.info.TypeInformationKind;
import saker.maven.support.impl.MavenImplUtils;
import saker.maven.support.main.configuration.option.MavenConfigurationTaskOption;
import saker.maven.support.main.dependency.ResolveMavenDependencyTaskFactory;
import saker.maven.support.thirdparty.org.eclipse.aether.repository.RepositoryPolicy;
import saker.nest.scriptinfo.reflection.annot.NestFieldInformation;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;

public class TaskDocs {
	private static final String FROM_MAVEN_DOC_NOTICE = "(From Maven documentation: https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#Dependency_Scope)";

	public static final String PARAM_CONFIGURATION = "Specifies the configuration to use during the operation.\n"
			+ "The parameter can be used to specify the local repository path, and the configuration for remote repositories.\n"
			+ "If no configuration is specified, the task will use the local repository at {user.home}/.m2/repository, and "
			+ "the remote repository from Maven Central: " + MavenImplUtils.MAVEN_CENTRAL_REPOSITORY_URL;
	private static final String OUTPUT_CONFIGURATION = "The Maven configuration that was used during the operation.\n"
			+ "This field can be passed to other Maven related tasks for their configurations.";

	@NestInformation("Represents a layout configuration for Maven repositories.\n"
			+ "Corresponds to the <layout/> element in a pom.xml.")
	@NestTypeInformation(kind = TypeInformationKind.ENUM,
			qualifiedName = "MavenRepositoryLayout",
			enumValues = {

					@NestFieldInformation(value = "default",
							info = @NestInformation("The default Maven respository layout used by Maven 2.x and 3.x.")),
					@NestFieldInformation(value = "legacy",
							info = @NestInformation("The Maven repository layout used by version 1.x.")),

			})
	public static class DocRepositoryLayout {
	}

	@NestInformation("Represents a checksum policy configuration for Maven artifacts.\n"
			+ "Corresponds to the <checksumPolicy/> element in a pom.xml.")
	@NestTypeInformation(kind = TypeInformationKind.ENUM,
			qualifiedName = "ChecksumPolicy",
			enumValues = {

					@NestFieldInformation(value = RepositoryPolicy.CHECKSUM_POLICY_FAIL,
							info = @NestInformation("The operation should fail is mismatching artifact checksums are detected.")),
					@NestFieldInformation(value = RepositoryPolicy.CHECKSUM_POLICY_IGNORE,
							info = @NestInformation("The operation should ignore mismatching artifact checksums.")),
					@NestFieldInformation(value = RepositoryPolicy.CHECKSUM_POLICY_WARN,
							info = @NestInformation("The operation should warn if mismatching artifact checksums are detected.")),

			})
	public static class DocChecksumPolicy {
	}

	@NestInformation("Represents an update policy configuration for Maven artifacts.\n"
			+ "Corresponds to the <updatePolicy/> element in a pom.xml.")
	@NestTypeInformation(kind = TypeInformationKind.LITERAL,
			qualifiedName = "UpdatePolicy",
			enumValues = {

					@NestFieldInformation(value = RepositoryPolicy.UPDATE_POLICY_ALWAYS,
							info = @NestInformation("The operation should always check if artifacts are up to date.")),
					@NestFieldInformation(value = RepositoryPolicy.UPDATE_POLICY_DAILY,
							info = @NestInformation("The operation should check if an artifact is up to date using daily intervals.")),
					@NestFieldInformation(value = RepositoryPolicy.UPDATE_POLICY_NEVER,
							info = @NestInformation("The operation should never check if an artifact is up to date.")),
					@NestFieldInformation(value = RepositoryPolicy.UPDATE_POLICY_INTERVAL,
							info = @NestInformation("The operation should check if the artifact is up to data by the specified interval.\n"
									+ "The value is expected to have the interval:<num-minutes> format.")),

			})
	public static class DocUpdatePolicy {
	}

	private static final String INFO_ARTIFACT_COORDINATES = "Represents Maven artifact coordinates in the format of <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>.";

	// The difference between DocInputArtifactCoordinates and DocOutputArtifactCoordinates is that the output artifact coordinates
	//   can be subscripted for their individual components. However, the input coordinates cannot be specified by
	//   their individual components.

	@NestInformation(INFO_ARTIFACT_COORDINATES)
	@NestTypeInformation(qualifiedName = "saker.maven.support.api.ArtifactCoordinates")
	public static class DocInputArtifactCoordinates {
	}

	@NestInformation(INFO_ARTIFACT_COORDINATES)
	@NestTypeInformation(qualifiedName = "saker.maven.support.api.ArtifactCoordinates")
	@NestFieldInformation(value = "GroupId",
			type = @NestTypeUsage(String.class),
			info = @NestInformation("The group identifier of the artifact. E.g. \"org.apache.maven\"."))
	@NestFieldInformation(value = "ArtifactId",
			type = @NestTypeUsage(String.class),
			info = @NestInformation("The artifact identifier of the artifact. E.g. \"maven-model\"."))
	@NestFieldInformation(value = "Classifier",
			type = @NestTypeUsage(String.class),
			info = @NestInformation("The classifier of the artifact. E.g. \"sources\"."))
	@NestFieldInformation(value = "Extension",
			type = @NestTypeUsage(String.class),
			info = @NestInformation("The extension of the artifact. E.g. \"jar\"."))
	@NestFieldInformation(value = "Version",
			type = @NestTypeUsage(String.class),
			info = @NestInformation("The version of the artifact. E.g. \"1.0.1\".\n"
					+ "The version can contain additional components such as \"1.0.1-SNAPSHOT\"."))
	public static class DocOutputArtifactCoordinates {
	}

	@NestInformation("The scope of an artifact dependency.\n"
			+ "The values may be scopes recognized by the Maven resolver.")
	@NestTypeInformation(kind = TypeInformationKind.LITERAL,
			qualifiedName = "MavenDependencyScope",
			enumValues = {

					@NestFieldInformation(value = "compile",
							info = @NestInformation("This is the default scope, used if none is specified. "
									+ "Compile dependencies are available in all classpaths of a project. "
									+ "Furthermore, those dependencies are propagated to dependent projects.\n"
									+ FROM_MAVEN_DOC_NOTICE)),
					@NestFieldInformation(value = "provided",
							info = @NestInformation("This is much like compile, but indicates you expect the JDK or a container to provide the dependency at runtime. "
									+ "This scope is only available on the compilation and test classpath, and is not transitive.\n"
									+ FROM_MAVEN_DOC_NOTICE)),
					@NestFieldInformation(value = "runtime",
							info = @NestInformation("This scope indicates that the dependency is not required for compilation, but is for execution. "
									+ "It is in the runtime and test classpaths, but not the compile classpath.\n"
									+ FROM_MAVEN_DOC_NOTICE)),
					@NestFieldInformation(value = "test",
							info = @NestInformation("This scope indicates that the dependency is not required for normal use of the application, "
									+ "and is only available for the test compilation and execution phases. This scope is not transitive.\n"
									+ FROM_MAVEN_DOC_NOTICE)),
					@NestFieldInformation(value = "system",
							info = @NestInformation("This scope is similar to provided except that you have to provide the JAR which contains it explicitly. "
									+ "The artifact is always available and is not looked up in a repository.\n"
									+ FROM_MAVEN_DOC_NOTICE)),

			})
	public static class DocDependencyScope {
	}

	@NestInformation("Represents the output of a Maven dependency resolution.\n" + "Output of the task "
			+ ResolveMavenDependencyTaskFactory.TASK_NAME + "().\n"
			+ "The results are recommended to be passed to other tasks that can properly convert it to the expected "
			+ "inputs of other tasks. (E.g. classpath, downloading, etc...)")
	@NestTypeInformation(qualifiedName = "saker.maven.support.api.dependency.MavenDependencyResolutionTaskOutput")
	@NestFieldInformation(value = "Configuration",
			type = @NestTypeUsage(MavenConfigurationTaskOption.class),
			info = @NestInformation(OUTPUT_CONFIGURATION))
	@NestFieldInformation(value = "ArtifactCoordinates",
			type = @NestTypeUsage(value = Collection.class, elementTypes = DocOutputArtifactCoordinates.class),
			info = @NestInformation("Gets the artifact coordinates of the resolved artifacts."))
	@NestFieldInformation(value = "ResolvedArtifacts",
			type = @NestTypeUsage(value = Collection.class, elementTypes = DocResolvedDependencyArtifact.class),
			info = @NestInformation("Gets the information about each resolved artifact.\n"
					+ "The elements contain information about the artifact coordinates and the scope of the resolved dependency."))
	@NestFieldInformation(value = "Scopes",
			type = @NestTypeUsage(DocMavenDependencyResolutionScopesOutput.class),
			info = @NestInformation("Gets a lookup object that can be used to retrieve views that contain resolved dependencies only for a given scope.\n"
					+ "Retrieving the appropriate fields will return the task output for the given scope of dependencies."))
	public static class DocMavenDependencyResolutionTaskOutput {

	}

	@NestTypeInformation(kind = TypeInformationKind.OBJECT,
			qualifiedName = "saker.maven.support.api.dependency.MavenDependencyResolutionScopesOutput")
	@NestInformation("Scope lookup object of a Maven dependency resolution output.\n"
			+ "Use the fields to retrieve the dependency only for the specified scopes.\n"
			+ "Apart from the declared ones, the object provides fields for "
			+ "dynamic scope collections in the format of scope[|scopes]*. When multiple scopes are specified separated by | character, then the artifacts "
			+ "available through any of the scopes will be part of the result.")
	@NestFieldInformation(value = "Compilation",
			type = @NestTypeUsage(DocMavenDependencyResolutionTaskOutput.class),
			info = @NestInformation("Gets the resolved dependencies to be passed for compilation.\n"
					+ "Retrieves the resolved artifacts available through the compile and provided scopes."))
	@NestFieldInformation(value = "Execution",
			type = @NestTypeUsage(DocMavenDependencyResolutionTaskOutput.class),
			info = @NestInformation("Gets the resolved dependencies to be passed for execution.\n"
					+ "Retrieves the resolved artifacts available through the compile and runtime scopes."))

	@NestFieldInformation(value = "TestCompilation",
			type = @NestTypeUsage(DocMavenDependencyResolutionTaskOutput.class),
			info = @NestInformation("Gets the resolved dependencies to be passed for test compilation.\n"
					+ "Retrieves the resolved artifacts available through the test, compile and provided scopes."))
	@NestFieldInformation(value = "TestExecution",
			type = @NestTypeUsage(DocMavenDependencyResolutionTaskOutput.class),
			info = @NestInformation("Gets the resolved dependencies to be passed for test execution.\n"
					+ "Retrieves the resolved artifacts available through the test, compile and runtime scopes."))
	public static class DocMavenDependencyResolutionScopesOutput {
	}

	@NestTypeInformation(qualifiedName = "saker.maven.support.api.dependency.ResolvedDependencyArtifact")
	@NestInformation("Represents a resolved Maven dependency for a single artifact.\n"
			+ "Contains the artifact coordinates and the scope of the dependency.")
	@NestFieldInformation(value = "Coordinates",
			type = @NestTypeUsage(DocOutputArtifactCoordinates.class),
			info = @NestInformation("Gets the artifact coordinates of the resolved artifacts."))
	@NestFieldInformation(value = "Scope",
			type = @NestTypeUsage(DocDependencyScope.class),
			info = @NestInformation("Gets the dependency scope through which the artifact was resolved."))
	@NestFieldInformation(value = "Configuration", info = @NestInformation(OUTPUT_CONFIGURATION))
	public static class DocResolvedDependencyArtifact {
	}

	@NestTypeInformation(qualifiedName = "saker.maven.support.api.localize.ArtifactLocalizationTaskOutput")
	@NestInformation("Output of the Maven artifact localization task.\n"
			+ "Provides access to the local paths of the localized artifacts.")
	@NestFieldInformation(value = "Configuration", info = @NestInformation(OUTPUT_CONFIGURATION))
	@NestFieldInformation(value = "ArtifactLocalPaths",
			type = @NestTypeUsage(value = Collection.class, elementTypes = SakerPath.class),
			info = @NestInformation("List of local file system paths of the localized artifacts."))
	@NestFieldInformation(value = "LocalizationResults",
			type = @NestTypeUsage(value = Collection.class,
					elementTypes = DocArtifactLocalizationWorkerTaskOutput.class),
			info = @NestInformation("List of localization results for each localized artifact.\n"
					+ "Each result element provides access to the coordinates and local path of the artifact."))
	@NestFieldInformation(value = "Coordinates",
			type = @NestTypeUsage(value = Collection.class, elementTypes = DocOutputArtifactCoordinates.class),
			info = @NestInformation("List of the coordinates of the localized artifacts."))
	public static class DocArtifactLocalizationTaskOutput {
	}

	@NestTypeInformation(qualifiedName = "saker.maven.support.api.localize.ArtifactLocalizationWorkerTaskOutput")
	@NestInformation("Output of a single artifact localization operation.")
	@NestFieldInformation(value = "Coordinates",
			type = @NestTypeUsage(DocOutputArtifactCoordinates.class),
			info = @NestInformation("The localized artifact coordinates."))
	@NestFieldInformation(value = "LocalPath",
			type = @NestTypeUsage(SakerPath.class),
			info = @NestInformation("The local file system path that is the result of the artifact localization."))
	public static class DocArtifactLocalizationWorkerTaskOutput {
	}

	@NestTypeInformation(qualifiedName = "saker.maven.support.api.download.ArtifactDownloadTaskOutput")
	@NestInformation("Output of the Maven artifact download task.\n"
			+ "Provides access to the execution paths of the downloaded artifacts.")
	@NestFieldInformation(value = "Configuration", info = @NestInformation(OUTPUT_CONFIGURATION))
	@NestFieldInformation(value = "ArtifactPaths",
			type = @NestTypeUsage(value = Collection.class, elementTypes = SakerPath.class),
			info = @NestInformation("List of execution paths of the downloaded artifacts."))
	@NestFieldInformation(value = "DownloadResults",
			type = @NestTypeUsage(value = Collection.class, elementTypes = DocArtifactDownloadWorkerTaskOutput.class),
			info = @NestInformation("List of download results for each downloaded artifact.\n"
					+ "Each result element provides access to the coordinates and execution path of the artifact."))
	@NestFieldInformation(value = "Coordinates",
			type = @NestTypeUsage(value = Collection.class, elementTypes = DocOutputArtifactCoordinates.class),
			info = @NestInformation("List of the coordinates of the downloaded artifacts."))
	public static class DocArtifactDownloadTaskOutput {
	}

	@NestTypeInformation(qualifiedName = "saker.maven.support.api.download.ArtifactDownloadWorkerTaskOutput")
	@NestInformation("Output of a single artifact download operation.")
	@NestFieldInformation(value = "Coordinates",
			type = @NestTypeUsage(DocOutputArtifactCoordinates.class),
			info = @NestInformation("The downloaded artifact coordinates."))
	@NestFieldInformation(value = "Path",
			type = @NestTypeUsage(SakerPath.class),
			info = @NestInformation("The execution path that is the result of the artifact downloading ."))
	public static class DocArtifactDownloadWorkerTaskOutput {
	}

	@NestTypeInformation(
			qualifiedName = "saker.maven.support.api.MavenOperationConfiguration.AccountAuthenticationConfiguration")
	@NestInformation("Holds username-password pair for authenticating with Maven repositories.")
	public static class DocAccountAuthenticationConfiguration {
	}

	@NestTypeInformation(
			qualifiedName = "saker.maven.support.api.MavenOperationConfiguration.PrivateKeyAuthenticationConfiguration")
	@NestInformation("Holds a private key and pass phrase pair for authenticating with Maven repositories.\n"
			+ "Only the local file system path is stored in this configuration.")
	public static class DocPrivateKeyAuthenticationConfiguration {
	}

	@NestTypeInformation(qualifiedName = "saker.maven.support.api.install.ArtifactInstallWorkerTaskOutput")
	@NestInformation("Result of a Maven artifact installation task.")
	@NestFieldInformation(value = "ArtifactLocalPath",
			type = @NestTypeUsage(SakerPath.class),
			info = @NestInformation("The local path of the installed artifact.\n"
					+ "The path is to be interpreted on the local file system and points to the location of the "
					+ "artifact in the Maven repository to which it was installed to."))
	@NestFieldInformation(value = "Coordinates",
			type = @NestTypeUsage(DocOutputArtifactCoordinates.class),
			info = @NestInformation("The artifact coordinates of the installed artifact."))
	public static class DocArtifactInstallWorkerTaskOutput {
	}

	@NestTypeInformation(qualifiedName = "saker.maven.support.api.deploy.ArtifactDeployWorkerTaskOutput")
	@NestInformation("Result of a Maven artifact deployment task.")
	@NestFieldInformation(value = "Coordinates",
			type = @NestTypeUsage(DocOutputArtifactCoordinates.class),
			info = @NestInformation("The artifact coordinates of the deployed artifacts.\n"
					+ "The coordinates doesn't contain classifier and extension."))
	public static class DocArtifactDeployWorkerTaskOutput {
	}

	@NestTypeInformation(qualifiedName = "DeploymentSpecifier")
	@NestInformation("Artifact deployment specifier that contains the classifier and extension for a given artifact.\n"
			+ "The specifier is expected to be in the [<classifier>:]<extension> format.\n"
			+ "The classifier may be omitted. If the extension is specified as empty, \"jar\" will be used as a default.\n"
			+ "Some examples are: jar, pom, sources:jar, javadoc:jar.")
	public static class DocDeploymentSpecifier {
	}

	@NestTypeInformation(qualifiedName = "SakerPath")
	@NestInformation("Path to the Maven artifact that is being deployed.")
	public static class DocDeployArtifactPath {
	}

	@NestTypeInformation(qualifiedName = "SakerPath")
	@NestInformation("Path to the Maven artifact that is being installed.")
	public static class DocInstallArtifactPath {
	}

	@NestTypeInformation(qualifiedName = "SakerPath")
	@NestInformation("Local file system path to a Maven repository.")
	public static class DocLocalRepositoryPath {
	}

	@NestTypeInformation(qualifiedName = "SakerPath")
	@NestInformation("Execution path to a Maven pom.xml.")
	public static class DocMavenPomPath {
	}

	@NestTypeInformation(qualifiedName = "SakerPath")
	@NestInformation("Local file system path to a private key store that can be used to authenticate to Maven remote repositories.")
	public static class DocPrivateKeyLocalPath {
	}
}
