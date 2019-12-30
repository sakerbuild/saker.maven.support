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
