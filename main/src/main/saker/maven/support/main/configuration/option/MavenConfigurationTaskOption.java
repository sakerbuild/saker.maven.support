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
package saker.maven.support.main.configuration.option;

import java.util.Collection;

import saker.build.file.path.SakerPath;
import saker.maven.support.api.MavenOperationConfiguration;
import saker.maven.support.impl.MavenImplUtils;
import saker.maven.support.main.TaskDocs.DocLocalRepositoryPath;
import saker.nest.scriptinfo.reflection.annot.NestFieldInformation;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;

@NestInformation("Represents a Maven configuration used during operations with the Maven Resolver.\n"
		+ "The configuration contains options for defining the LocalRepositoryPath, and the remote "
		+ "Repositories to work with.")
@NestFieldInformation(value = "LocalRepositoryPath",
		type = @NestTypeUsage(DocLocalRepositoryPath.class),
		info = @NestInformation("Specifies the path on the local file system that should be used as the local repository of artifacts.\n"
				+ "The local repository serves as a cache for remote artifacts, and a local storage of private artifacts.\n"
				+ "If not specified, {user.home}/.m2/repository is used."))
@NestFieldInformation(value = "Repositories",
		type = @NestTypeUsage(value = Collection.class, elementTypes = RepositoryTaskOption.class),
		info = @NestInformation("Specifies the remote repositories to use during operations with the Maven Resolver.\n"
				+ "If not specified, Maven Central is used with the Id of \"central\" at: "
				+ MavenImplUtils.MAVEN_CENTRAL_REPOSITORY_URL + "\n"
				+ "In order to remove the default, specify empty Repositories. If you specify any repository, the default central repository "
				+ "is not added automatically, and you need to add it yourself."))
public interface MavenConfigurationTaskOption {
	public default MavenOperationConfiguration createConfiguration() {
		return MavenOperationConfigurationTaskOptionUtils.createConfiguration(this);
	}

	//XXX we should accept LocalFileLocation as well
	public default SakerPath getLocalRepositoryPath() {
		return null;
	}

	public default Collection<RepositoryTaskOption> getRepositories() {
		return null;
	}

	public static MavenConfigurationTaskOption valueOf(MavenOperationConfiguration configuration) {
		return new MavenConfigurationTaskOption() {
			@Override
			public MavenOperationConfiguration createConfiguration() {
				return configuration;
			}
		};
	}
}