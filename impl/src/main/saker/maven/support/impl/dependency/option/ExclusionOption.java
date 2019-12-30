package saker.maven.support.impl.dependency.option;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import saker.build.thirdparty.saker.util.ObjectUtils;

public final class ExclusionOption implements Externalizable {
	private static final long serialVersionUID = 1L;

	private static final Pattern COORDINATE_PATTERN = Pattern.compile("([^: ]+)(:([^: ]+)(:([^: ]*)(:([^: ]+))?)?)?");

	private String groupId;
	private String artifactId;
	private String classifier;
	private String extension;

	/**
	 * For {@link Externalizable}.
	 */
	public ExclusionOption() {
	}

	public ExclusionOption(String groupId, String artifactId, String classifier, String extension) {
		this.groupId = groupId;
		this.artifactId = artifactId;
		this.classifier = classifier;
		this.extension = extension;
	}

	public static ExclusionOption valueOf(String coordinates) {
		Objects.requireNonNull(coordinates, "coordinates");

		Matcher m = COORDINATE_PATTERN.matcher(coordinates);
		if (!m.matches()) {
			throw new IllegalArgumentException("Bad exclusion coordinates " + coordinates
					+ ", expected format is <groupId>[:<artifactId>[:<classifier>[:<extension>]]]");
		}
		String groupId = m.group(1);
		String artifactId = m.group(3);
		String classifier = getGroupValueOrDefault(m.group(5), null);
		String extension = getGroupValueOrDefault(m.group(7), null);
		return new ExclusionOption(groupId, artifactId, classifier, extension);
	}

	public String getGroupId() {
		return groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public String getClassifier() {
		return classifier;
	}

	public String getExtension() {
		return extension;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(groupId);
		out.writeObject(artifactId);
		out.writeObject(classifier);
		out.writeObject(extension);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		groupId = (String) in.readObject();
		artifactId = (String) in.readObject();
		classifier = (String) in.readObject();
		extension = (String) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((artifactId == null) ? 0 : artifactId.hashCode());
		result = prime * result + ((classifier == null) ? 0 : classifier.hashCode());
		result = prime * result + ((extension == null) ? 0 : extension.hashCode());
		result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
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
		ExclusionOption other = (ExclusionOption) obj;
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
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(groupId);
		if (!ObjectUtils.isNullOrEmpty(artifactId)) {
			sb.append(':');
			sb.append(artifactId);
		}
		if (!ObjectUtils.isNullOrEmpty(extension)) {
			sb.append(':');
			sb.append(extension);
		}
		if (!ObjectUtils.isNullOrEmpty(classifier)) {
			sb.append(':');
			sb.append(classifier);
		}
		return sb.toString();
	}

	private static String getGroupValueOrDefault(String value, String defaultValue) {
		return (value == null || value.length() <= 0) ? defaultValue : value;
	}
}
