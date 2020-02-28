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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.maven.support.api.dependency.MavenDependencyResolutionExtensionsOutput;
import saker.maven.support.api.dependency.MavenDependencyResolutionTaskOutput;
import saker.maven.support.api.dependency.ResolvedDependencyArtifact;

public class MavenDependencyResolutionExtensionsOutputImpl
		implements MavenDependencyResolutionExtensionsOutput, Externalizable {
	private static final long serialVersionUID = 1L;

	private static final Pattern PATTERN_SPLIT_OR = Pattern.compile("[| ]+");

	private MavenDependencyResolutionTaskOutputImpl resolutionOutput;

	/**
	 * For {@link Externalizable}.
	 */
	public MavenDependencyResolutionExtensionsOutputImpl() {
	}

	public MavenDependencyResolutionExtensionsOutputImpl(MavenDependencyResolutionTaskOutputImpl resolutionOutput) {
		this.resolutionOutput = resolutionOutput;
	}

	@Override
	public MavenDependencyResolutionTaskOutput get(String extension) {
		if (extension == null) {
			return new MavenDependencyResolutionTaskOutputImpl(resolutionOutput.getConfiguration(),
					Collections.emptySet());
		}

		Set<String> acceptextensions = ObjectUtils.newHashSet(PATTERN_SPLIT_OR.split(extension));
		acceptextensions.remove("");

		Set<ResolvedDependencyArtifact> artifacts = new LinkedHashSet<>();
		for (ResolvedDependencyArtifact art : resolutionOutput.getResolvedArtifacts()) {
			if (acceptextensions.contains(art.getCoordinates().getExtension())) {
				artifacts.add(art);
			}
		}
		return new MavenDependencyResolutionTaskOutputImpl(resolutionOutput.getConfiguration(), artifacts);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(resolutionOutput);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		resolutionOutput = (MavenDependencyResolutionTaskOutputImpl) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((resolutionOutput == null) ? 0 : resolutionOutput.hashCode());
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
		MavenDependencyResolutionExtensionsOutputImpl other = (MavenDependencyResolutionExtensionsOutputImpl) obj;
		if (resolutionOutput == null) {
			if (other.resolutionOutput != null)
				return false;
		} else if (!resolutionOutput.equals(other.resolutionOutput))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + resolutionOutput + "]";
	}
}
