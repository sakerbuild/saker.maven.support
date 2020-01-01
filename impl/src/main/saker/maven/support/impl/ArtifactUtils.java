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
package saker.maven.support.impl;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.maven.support.api.ArtifactCoordinates;
import saker.maven.support.thirdparty.org.eclipse.aether.artifact.Artifact;
import saker.maven.support.thirdparty.org.eclipse.aether.artifact.DefaultArtifact;

public class ArtifactUtils {

	private ArtifactUtils() {
		throw new UnsupportedOperationException();
	}

	public static Artifact toArtifact(ArtifactCoordinates acoords) {
		return new DefaultArtifact(acoords.getGroupId(), acoords.getArtifactId(), acoords.getClassifier(),
				acoords.getExtension(), acoords.getVersion());
	}

	public static ArtifactCoordinates toArtifactCoordinates(Artifact artifact) {
		return new ArtifactCoordinates(artifact.getGroupId(), artifact.getArtifactId(), artifact.getClassifier(),
				artifact.getExtension(), artifact.getVersion());
	}

	public static ArtifactCoordinates toArtifactCoordinatesWithClassifier(Artifact artifact, String classifier) {
		return new ArtifactCoordinates(artifact.getGroupId(), artifact.getArtifactId(), classifier,
				artifact.getExtension(), artifact.getVersion());
	}

	public static ArtifactCoordinates toArtifactCoordinatesWithClassifier(ArtifactCoordinates artifact,
			String classifier) {
		return new ArtifactCoordinates(artifact.getGroupId(), artifact.getArtifactId(), classifier,
				artifact.getExtension(), artifact.getVersion());
	}

	public static String toArtifactCoordinateString(Artifact artifact) {
		StringBuilder sb = new StringBuilder();
		sb.append(artifact.getGroupId());
		sb.append(':');
		sb.append(artifact.getArtifactId());
		String extension = artifact.getExtension();
		String classifier = artifact.getClassifier();
		if (!ObjectUtils.isNullOrEmpty(extension)) {
			sb.append(':');
			sb.append(extension);
		}
		if (!ObjectUtils.isNullOrEmpty(classifier)) {
			sb.append(':');
			sb.append(classifier);
		}
		sb.append(':');
		sb.append(artifact.getVersion());
		return sb.toString();
	}

}
