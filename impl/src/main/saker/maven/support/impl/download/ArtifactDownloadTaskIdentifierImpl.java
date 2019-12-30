package saker.maven.support.impl.download;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.file.path.SakerPath;
import saker.build.task.identifier.TaskIdentifier;

public class ArtifactDownloadTaskIdentifierImpl implements TaskIdentifier, Externalizable {
	private static final long serialVersionUID = 1L;

	private String workingLocation;
	private SakerPath repositoryRelativeArtifactPath;

	/**
	 * For {@link Externalizable}.
	 */
	public ArtifactDownloadTaskIdentifierImpl() {
	}

	public ArtifactDownloadTaskIdentifierImpl(String workinglocation, SakerPath repositoryRelativeArtifactPath) {
		this.workingLocation = workinglocation;
		this.repositoryRelativeArtifactPath = repositoryRelativeArtifactPath;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(workingLocation);
		out.writeObject(repositoryRelativeArtifactPath);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		workingLocation = (String) in.readObject();
		repositoryRelativeArtifactPath = (SakerPath) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((workingLocation == null) ? 0 : workingLocation.hashCode());
		result = prime * result
				+ ((repositoryRelativeArtifactPath == null) ? 0 : repositoryRelativeArtifactPath.hashCode());
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
		ArtifactDownloadTaskIdentifierImpl other = (ArtifactDownloadTaskIdentifierImpl) obj;
		if (workingLocation == null) {
			if (other.workingLocation != null)
				return false;
		} else if (!workingLocation.equals(other.workingLocation))
			return false;
		if (repositoryRelativeArtifactPath == null) {
			if (other.repositoryRelativeArtifactPath != null)
				return false;
		} else if (!repositoryRelativeArtifactPath.equals(other.repositoryRelativeArtifactPath))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "["
				+ (workingLocation != null ? "workingLocation=" + workingLocation + ", " : "")
				+ (repositoryRelativeArtifactPath != null ? "repositoryRelativePath=" + repositoryRelativeArtifactPath
						: "")
				+ "]";
	}

}
