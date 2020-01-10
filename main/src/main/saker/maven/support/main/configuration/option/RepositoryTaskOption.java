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

import saker.maven.support.main.TaskDocs.DocRepositoryLayout;
import saker.nest.scriptinfo.reflection.annot.NestFieldInformation;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;

@NestInformation("Represents a remote repository configuration for Maven operations.\n"
		+ "The configuration contains options for specifying how the repository should be interacted with.\n"
		+ "The configuration corresponds to the <repository/> element in a pom.xml.")
@NestFieldInformation(value = "Id",
		type = @NestTypeUsage(String.class),
		info = @NestInformation("Specifies the identifier of the repository configuration.\n"
				+ "The identifier should uniquely identify the repository configuration in the associated context. "
				+ "Serves the same purpose as the <id/> element in the pom.xml <repository/> configuration."))
@NestFieldInformation(value = "Url",
		type = @NestTypeUsage(String.class),
		info = @NestInformation("Specifies the location and transfer protocol to interact with the repository."))
@NestFieldInformation(value = "Layout",
		type = @NestTypeUsage(DocRepositoryLayout.class),
		info = @NestInformation("Specifies the layout structure of the repository.\n"
				+ "Either \"default\" or \"legacy\". The legacy layout is used by Maven 1.x. The default is \"default\".\n"
				+ "Corresponds to the <layout/> element in the pom.xml."))
@NestFieldInformation(value = "Snapshots",
		type = @NestTypeUsage(RepositoryPolicyTaskOption.class),
		info = @NestInformation("Specifies the snapshot policy of the repository configuration.\n"
				+ "The option defines how snapshot artifacts should be handled.\n"
				+ "Corresponds to the <snapshots/> element in the pom.xml."))
@NestFieldInformation(value = "Releases",
		type = @NestTypeUsage(RepositoryPolicyTaskOption.class),
		info = @NestInformation("Specifies the release policy of the repository configuration.\n"
				+ "The option defines how release artifacts should be handled.\n"
				+ "Corresponds to the <releases/> element in the pom.xml."))
public interface RepositoryTaskOption {
	public String getId();

	public String getUrl();

	public String getLayout();

	public RepositoryPolicyTaskOption getSnapshots();

	public RepositoryPolicyTaskOption getReleases();

	public AuthenticationTaskOption getAuthentication();
	
	//TODO handle RepositoryConfiguration
}