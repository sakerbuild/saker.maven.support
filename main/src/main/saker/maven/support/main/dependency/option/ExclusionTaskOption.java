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
package saker.maven.support.main.dependency.option;

import saker.nest.scriptinfo.reflection.annot.NestFieldInformation;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;

@NestInformation("Specifies an artifact exclusion for transitive dependencies.\n"
		+ "Corresponds to the <exclusion/> element in a pom.xml.\n"
		+ "The option also accepts coordinates in the format of: <groupId>[:<artifactId>[:<classifier>[:<extension>]]].")
@NestFieldInformation(value = "GroupId",
		type = @NestTypeUsage(String.class),
		info = @NestInformation("The Group Id part of the excluded dependency.\n"
				+ "Corresponds to the <groupId/> element in <exclusion/> in a pom.xml."))
@NestFieldInformation(value = "ArtifactId",
		type = @NestTypeUsage(String.class),
		info = @NestInformation("The Artifact Id part of the excluded dependency.\n"
				+ "Corresponds to the <artifactId/> element in <exclusion/> in a pom.xml."))
@NestFieldInformation(value = "Classifier",
		type = @NestTypeUsage(String.class),
		info = @NestInformation("The Classifier part of the excluded dependency.\n"
				+ "This has no corresponding element in a pom.xml, however, the Maven Resolver library accepts it as part of an exclusion."))
@NestFieldInformation(value = "Extension",
		type = @NestTypeUsage(String.class),
		info = @NestInformation("The Extension part of the excluded dependency.\n"
				+ "This has no corresponding element in a pom.xml, however, the Maven Resolver library accepts it as part of an exclusion."))
public interface ExclusionTaskOption {
	//null is treated as "*"
	public String getGroupId();

	//null is treated as "*"
	public String getArtifactId();

	//null is treated as "*"
	public String getClassifier();

	//null is treated as "*"
	public String getExtension();

	public static ExclusionTaskOption valueOf(String coordinates) {
		return SimpleExclusionTaskOption.valueOf(coordinates);
	}
}