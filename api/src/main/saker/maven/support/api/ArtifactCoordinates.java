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
package saker.maven.support.api;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saker.build.thirdparty.saker.util.ObjectUtils;

/**
 * Represents the coordinates of a Maven artifact.
 * <p>
 * The coordinates consist of the following:
 * <ul>
 * <li>{@linkplain #getGroupId() Group ID}</li>
 * <li>{@linkplain #getArtifactId() Artifact ID}</li>
 * <li>{@linkplain #getClassifier() Classifier}</li>
 * <li>{@linkplain #getExtension() Extension}</li>
 * <li>{@linkplain #getVersion() Version}</li>
 * </ul>
 * <p>
 * The artifact coordinates are an unique identifier for a Maven artifact. The class only stores the coordinates, but no
 * other information like repository or file path.
 * <p>
 * Users can use the {@linkplain ArtifactCoordinates#ArtifactCoordinates(String, String, String, String, String)
 * constructor} or the {@link #valueOf(String)} method to create a new instance.
 */
public final class ArtifactCoordinates implements Externalizable {
	private static final long serialVersionUID = 1L;

	//from org.eclipse.aether.artifact.DefaultArtifact
	private static final Pattern COORDINATE_PATTERN = Pattern
			.compile("([^: ]+):([^: ]+)(:([^: ]*)(:([^: ]+))?)?:([^: ]+)");

	private String groupId;
	private String artifactId;
	private String classifier;
	private String extension;
	private String version;

	/**
	 * For {@link Externalizable}.
	 * 
	 * @deprecated Use the {@link #ArtifactCoordinates(String, String, String, String, String)} constructor or
	 *                 {@link #valueOf(String)}.
	 */
	@Deprecated
	public ArtifactCoordinates() {
	}

	/**
	 * Creates a new artifact coordinates with the specified fields.
	 * <p>
	 * The constructor doesn't replace <code>null</code> arguments with defaults. If <code>classifier</code> or
	 * <code>extension</code> are empty strings, they are <code>null</code>ified.
	 * 
	 * @param groupId
	 *            The group ID.
	 * @param artifactId
	 *            The artifact ID.
	 * @param classifier
	 *            The classifier.
	 * @param extension
	 *            The extension.
	 * @param version
	 *            The version.
	 * @throws NullPointerException
	 *             If the group ID, artifact ID or version are <code>null</code>.
	 * @throws IllegalArgumentException
	 *             If group ID, artifact ID, or version are empty strings.
	 */
	public ArtifactCoordinates(String groupId, String artifactId, String classifier, String extension, String version)
			throws NullPointerException, IllegalArgumentException {
		Objects.requireNonNull(groupId, "group id");
		Objects.requireNonNull(artifactId, "artifact id");
		Objects.requireNonNull(version, "version");
		if ("".equals(groupId)) {
			throw new IllegalArgumentException("Group ID is empty string.");
		}
		if ("".equals(artifactId)) {
			throw new IllegalArgumentException("Artifact ID is empty string.");
		}
		if ("".equals(version)) {
			throw new IllegalArgumentException("Version is empty string.");
		}
		if ("".equals(classifier)) {
			classifier = null;
		}
		if ("".equals(extension)) {
			extension = null;
		}

		this.groupId = groupId;
		this.artifactId = artifactId;
		this.classifier = classifier;
		this.extension = extension;
		this.version = version;
	}

	/**
	 * Parses the argument artifact coordinates and constructs a new instance.
	 * <p>
	 * The argument is expected to be in the following format:
	 * 
	 * <pre>
	 * &lt;groupId&gt;:&lt;artifactId&gt;[:&lt;extension&gt;[:&lt;classifier&gt;]]:&lt;version&gt;
	 * </pre>
	 * 
	 * If <code>extension</code> is not specified, it will be defaulted to <code>"jar"</code>.
	 * 
	 * @param coordinates
	 *            The artifact coordinates to parse.
	 * @return The created {@link ArtifactCoordinates} representation.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             If the argument has invalid format.
	 */
	public static ArtifactCoordinates valueOf(String coordinates)
			throws NullPointerException, IllegalArgumentException {
		Objects.requireNonNull(coordinates, "coordinates");

		Matcher m = COORDINATE_PATTERN.matcher(coordinates);
		if (!m.matches()) {
			throw new IllegalArgumentException("Invalid artifact coordinates format: " + coordinates
					+ ", expected format is <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>");
		}
		String groupId = m.group(1);
		String artifactId = m.group(2);
		String extension = ObjectUtils.nullDefault(m.group(4), "jar");
		String classifier = m.group(6);
		String version = m.group(7);
		return new ArtifactCoordinates(groupId, artifactId, classifier, extension, version);
	}

	/**
	 * Gets the group identifier of this artifact, for example "org.apache.maven".
	 * 
	 * @return The group identifier, never {@code null}.
	 */
	public String getGroupId() {
		return groupId;
	}

	/**
	 * Gets the artifact identifier of this artifact, for example "maven-model".
	 * 
	 * @return The artifact identifier, never {@code null}.
	 */
	public String getArtifactId() {
		return artifactId;
	}

	/**
	 * Gets the classifier of this artifact, for example "sources".
	 * 
	 * @return The classifier or <code>null</code> if none.
	 */
	public String getClassifier() {
		return classifier;
	}

	/**
	 * Gets the (file) extension of this artifact, for example "jar" or "tar.gz".
	 * 
	 * @return The file extension (without leading period) or <code>null</code> if none.
	 */
	public String getExtension() {
		return extension;
	}

	/**
	 * Gets the version of this artifact, for example "1.0-20100529-1213".
	 * <p>
	 * Note that in case of meta versions like "1.0-SNAPSHOT", the artifact's version depends on the state of the
	 * artifact. Artifacts that have been resolved or deployed will usually have the meta version expanded.
	 * 
	 * @return The version, never {@code null}.
	 */
	public String getVersion() {
		return version;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(groupId);
		out.writeObject(artifactId);
		out.writeObject(classifier);
		out.writeObject(extension);
		out.writeObject(version);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		groupId = (String) in.readObject();
		artifactId = (String) in.readObject();
		classifier = (String) in.readObject();
		extension = (String) in.readObject();
		version = (String) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((artifactId == null) ? 0 : artifactId.hashCode());
		result = prime * result + ((classifier == null) ? 0 : classifier.hashCode());
		result = prime * result + ((extension == null) ? 0 : extension.hashCode());
		result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ArtifactCoordinates other = (ArtifactCoordinates) obj;
		if (artifactId == null) {
			if (other.artifactId != null)
				return false;
		} else if (!artifactId.equals(other.artifactId))
			return false;
		if (classifier == null) {
			if (other.classifier != null)
				return false;
		} else if (!classifier.equals(other.classifier))
			return false;
		if (extension == null) {
			if (other.extension != null)
				return false;
		} else if (!extension.equals(other.extension))
			return false;
		if (groupId == null) {
			if (other.groupId != null)
				return false;
		} else if (!groupId.equals(other.groupId))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(groupId);
		sb.append(':');
		sb.append(artifactId);
		if (!ObjectUtils.isNullOrEmpty(extension)) {
			sb.append(':');
			sb.append(extension);
		}
		if (!ObjectUtils.isNullOrEmpty(classifier)) {
			sb.append(':');
			sb.append(classifier);
		}
		sb.append(':');
		sb.append(version);
		return sb.toString();
	}
}
