package saker.maven.support.impl.dependency;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.maven.support.api.ArtifactCoordinates;
import saker.maven.support.api.dependency.ResolvedDependencyArtifact;

public class ResolvedDependencyArtifactImpl implements ResolvedDependencyArtifact, Externalizable {
	private static final long serialVersionUID = 1L;

	private ArtifactCoordinates coordinates;
	private String scope;

	/**
	 * For {@link Externalizable}.
	 */
	public ResolvedDependencyArtifactImpl() {
	}

	public ResolvedDependencyArtifactImpl(ArtifactCoordinates coordinates, String scope) {
		this.coordinates = coordinates;
		this.scope = scope;
	}

	@Override
	public ArtifactCoordinates getCoordinates() {
		return coordinates;
	}

	@Override
	public String getScope() {
		return scope;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(coordinates);
		out.writeObject(scope);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		coordinates = (ArtifactCoordinates) in.readObject();
		scope = (String) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((coordinates == null) ? 0 : coordinates.hashCode());
		result = prime * result + ((scope == null) ? 0 : scope.hashCode());
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
		ResolvedDependencyArtifactImpl other = (ResolvedDependencyArtifactImpl) obj;
		if (coordinates == null) {
			if (other.coordinates != null)
				return false;
		} else if (!coordinates.equals(other.coordinates))
			return false;
		if (scope == null) {
			if (other.scope != null)
				return false;
		} else if (!scope.equals(other.scope))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + coordinates + " " + scope + "]";
	}

}
