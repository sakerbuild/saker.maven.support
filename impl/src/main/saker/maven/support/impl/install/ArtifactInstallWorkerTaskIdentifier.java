package saker.maven.support.impl.install;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.task.identifier.TaskIdentifier;
import saker.maven.support.api.ArtifactCoordinates;
import saker.maven.support.api.MavenOperationConfiguration;

public class ArtifactInstallWorkerTaskIdentifier implements TaskIdentifier, Externalizable {
	private static final long serialVersionUID = 1L;

	private MavenOperationConfiguration configuration;
	private ArtifactCoordinates coordinates;

	/**
	 * For {@link Externalizable}.
	 */
	public ArtifactInstallWorkerTaskIdentifier() {
	}

	public ArtifactInstallWorkerTaskIdentifier(MavenOperationConfiguration configuration,
			ArtifactCoordinates coordinates) {
		this.configuration = configuration;
		this.coordinates = coordinates;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(configuration);
		out.writeObject(coordinates);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		configuration = (MavenOperationConfiguration) in.readObject();
		coordinates = (ArtifactCoordinates) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((configuration == null) ? 0 : configuration.hashCode());
		result = prime * result + ((coordinates == null) ? 0 : coordinates.hashCode());
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
		ArtifactInstallWorkerTaskIdentifier other = (ArtifactInstallWorkerTaskIdentifier) obj;
		if (configuration == null) {
			if (other.configuration != null)
				return false;
		} else if (!configuration.equals(other.configuration))
			return false;
		if (coordinates == null) {
			if (other.coordinates != null)
				return false;
		} else if (!coordinates.equals(other.coordinates))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (configuration != null ? "configuration=" + configuration + ", " : "")
				+ (coordinates != null ? "coordinates=" + coordinates : "") + "]";
	}

}
