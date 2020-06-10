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
package saker.maven.support.impl.dependency;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.maven.support.api.ArtifactCoordinates;
import saker.maven.support.api.MavenOperationConfiguration;
import saker.maven.support.api.dependency.ResolvedDependencyArtifact;

public class ResolvedDependencyArtifactImpl implements ResolvedDependencyArtifact, Externalizable {
	private static final long serialVersionUID = 1L;

	private ArtifactCoordinates coordinates;
	private String scope;
	private MavenOperationConfiguration configuration;

	/**
	 * For {@link Externalizable}.
	 */
	public ResolvedDependencyArtifactImpl() {
	}

	public ResolvedDependencyArtifactImpl(ArtifactCoordinates coordinates, String scope,
			MavenOperationConfiguration configuration) {
		this.coordinates = coordinates;
		this.scope = scope;
		this.configuration = configuration;
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
	public MavenOperationConfiguration getConfiguration() {
		return configuration;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(coordinates);
		out.writeObject(scope);
		out.writeObject(configuration);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		coordinates = (ArtifactCoordinates) in.readObject();
		scope = (String) in.readObject();
		configuration = (MavenOperationConfiguration) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((configuration == null) ? 0 : configuration.hashCode());
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
