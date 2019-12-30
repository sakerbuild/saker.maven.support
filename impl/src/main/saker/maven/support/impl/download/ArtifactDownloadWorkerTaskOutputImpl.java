package saker.maven.support.impl.download;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.file.content.ContentDescriptor;
import saker.build.file.path.SakerPath;
import saker.maven.support.api.ArtifactCoordinates;
import saker.maven.support.api.download.ArtifactDownloadWorkerTaskOutput;

public class ArtifactDownloadWorkerTaskOutputImpl implements ArtifactDownloadWorkerTaskOutput, Externalizable {
	private static final long serialVersionUID = 1L;

	private ArtifactCoordinates coordinates;
	private SakerPath path;
	private ContentDescriptor contentDescriptor;

	/**
	 * For {@link Externalizable}.
	 */
	public ArtifactDownloadWorkerTaskOutputImpl() {
	}

	public ArtifactDownloadWorkerTaskOutputImpl(ArtifactCoordinates coordinates, SakerPath path,
			ContentDescriptor contentDescriptor) {
		this.coordinates = coordinates;
		this.path = path;
		this.contentDescriptor = contentDescriptor;
	}

	@Override
	public ArtifactCoordinates getCoordinates() {
		return coordinates;
	}

	@Override
	public SakerPath getPath() {
		return path;
	}

	@Override
	public ContentDescriptor getContentDescriptor() {
		return contentDescriptor;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(path);
		out.writeObject(contentDescriptor);
		out.writeObject(coordinates);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		path = (SakerPath) in.readObject();
		contentDescriptor = (ContentDescriptor) in.readObject();
		coordinates = (ArtifactCoordinates) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((contentDescriptor == null) ? 0 : contentDescriptor.hashCode());
		result = prime * result + ((coordinates == null) ? 0 : coordinates.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
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
		ArtifactDownloadWorkerTaskOutputImpl other = (ArtifactDownloadWorkerTaskOutputImpl) obj;
		if (contentDescriptor == null) {
			if (other.contentDescriptor != null)
				return false;
		} else if (!contentDescriptor.equals(other.contentDescriptor))
			return false;
		if (coordinates == null) {
			if (other.coordinates != null)
				return false;
		} else if (!coordinates.equals(other.coordinates))
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + coordinates + " -> " + path + "]";
	}

}