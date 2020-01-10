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
package saker.maven.support.impl.deploy;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

import saker.build.task.identifier.TaskIdentifier;
import saker.maven.support.api.ArtifactCoordinates;
import saker.maven.support.api.MavenOperationConfiguration.RepositoryConfiguration;

public class ArtifactDeployWorkerTaskIdentifier implements TaskIdentifier, Externalizable {
	private static final long serialVersionUID = 1L;

	private RepositoryConfiguration remoteRepository;
	private ArtifactCoordinates coordinates;

	/**
	 * For {@link Externalizable}.
	 */
	public ArtifactDeployWorkerTaskIdentifier() {
	}

	public ArtifactDeployWorkerTaskIdentifier(RepositoryConfiguration configuration, ArtifactCoordinates coordinates) {
		Objects.requireNonNull(configuration, "repository configuration");
		Objects.requireNonNull(coordinates, "coordinates");
		if (coordinates.getExtension() != null) {
			throw new IllegalArgumentException("Deploy coordinates must not have extension.");
		}
		if (coordinates.getClassifier() != null) {
			throw new IllegalArgumentException("Deploy coordinates must not have classifier.");
		}
		this.remoteRepository = configuration;
		this.coordinates = coordinates;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(remoteRepository);
		out.writeObject(coordinates);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		remoteRepository = (RepositoryConfiguration) in.readObject();
		coordinates = (ArtifactCoordinates) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((remoteRepository == null) ? 0 : remoteRepository.hashCode());
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
		ArtifactDeployWorkerTaskIdentifier other = (ArtifactDeployWorkerTaskIdentifier) obj;
		if (remoteRepository == null) {
			if (other.remoteRepository != null)
				return false;
		} else if (!remoteRepository.equals(other.remoteRepository))
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
		return getClass().getSimpleName() + "["
				+ (remoteRepository != null ? "remoteRepository=" + remoteRepository + ", " : "")
				+ (coordinates != null ? "coordinates=" + coordinates : "") + "]";
	}

}
