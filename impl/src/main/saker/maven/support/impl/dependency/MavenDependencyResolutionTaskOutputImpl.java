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
import java.util.Collection;
import java.util.LinkedHashSet;

import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.maven.support.api.ArtifactCoordinates;
import saker.maven.support.api.MavenOperationConfiguration;
import saker.maven.support.api.dependency.MavenDependencyResolutionExtensionsOutput;
import saker.maven.support.api.dependency.MavenDependencyResolutionScopesOutput;
import saker.maven.support.api.dependency.MavenDependencyResolutionTaskOutput;
import saker.maven.support.api.dependency.ResolvedDependencyArtifact;

public class MavenDependencyResolutionTaskOutputImpl implements MavenDependencyResolutionTaskOutput, Externalizable {
	private static final long serialVersionUID = 1L;

	private MavenOperationConfiguration config;
	private Collection<ResolvedDependencyArtifact> resolvedArtifacts;

	/**
	 * For {@link Externalizable}.
	 */
	public MavenDependencyResolutionTaskOutputImpl() {
	}

	public MavenDependencyResolutionTaskOutputImpl(MavenOperationConfiguration config,
			Collection<ResolvedDependencyArtifact> resolvedArtifacts) {
		this.config = config;
		this.resolvedArtifacts = resolvedArtifacts;
	}

	@Override
	public MavenOperationConfiguration getConfiguration() {
		return config;
	}

	@Override
	public MavenDependencyResolutionScopesOutput getScopes() {
		return new MavenDependencyResolutionScopesOutputImpl(this);
	}

	@Override
	public MavenDependencyResolutionExtensionsOutput getExtensions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<ArtifactCoordinates> getArtifactCoordinates() {
		LinkedHashSet<ArtifactCoordinates> result = new LinkedHashSet<>();
		for (ResolvedDependencyArtifact artifact : resolvedArtifacts) {
			result.add(artifact.getCoordinates());
		}
		return result;
	}

	@Override
	public Collection<ResolvedDependencyArtifact> getResolvedArtifacts() {
		return resolvedArtifacts;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(config);
		SerialUtils.writeExternalCollection(out, resolvedArtifacts);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		config = (MavenOperationConfiguration) in.readObject();
		resolvedArtifacts = SerialUtils.readExternalImmutableLinkedHashSet(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((config == null) ? 0 : config.hashCode());
		result = prime * result + ((resolvedArtifacts == null) ? 0 : resolvedArtifacts.hashCode());
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
		MavenDependencyResolutionTaskOutputImpl other = (MavenDependencyResolutionTaskOutputImpl) obj;
		if (config == null) {
			if (other.config != null)
				return false;
		} else if (!config.equals(other.config))
			return false;
		if (resolvedArtifacts == null) {
			if (other.resolvedArtifacts != null)
				return false;
		} else if (!resolvedArtifacts.equals(other.resolvedArtifacts))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "["
				+ (resolvedArtifacts != null ? "resolvedArtifacts=" + resolvedArtifacts : "") + "]";
	}

}
