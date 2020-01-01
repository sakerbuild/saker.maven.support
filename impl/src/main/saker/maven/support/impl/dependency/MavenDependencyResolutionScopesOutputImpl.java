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

import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.maven.support.api.dependency.MavenDependencyResolutionScopesOutput;
import saker.maven.support.api.dependency.MavenDependencyResolutionTaskOutput;
import saker.maven.support.api.dependency.ResolvedDependencyArtifact;

public class MavenDependencyResolutionScopesOutputImpl
		implements MavenDependencyResolutionScopesOutput, Externalizable {
	private static final long serialVersionUID = 1L;

	private static final Pattern PATTERN_SPLIT_OR = Pattern.compile("[| ]+");

	private static final Set<String> SCOPES_COMPILE_RUNTIME = ImmutableUtils
			.makeImmutableNavigableSet(new String[] { "compile", "runtime" });
	private static final Set<String> SCOPES_COMPILE_PROVIDED = ImmutableUtils
			.makeImmutableNavigableSet(new String[] { "compile", "provided" });
	private static final Set<String> SCOPES_TEST_COMPILE_PROVIDED = ImmutableUtils
			.makeImmutableNavigableSet(new String[] { "compile", "provided", "test" });
	private static final Set<String> SCOPES_TEST_COMPILE_RUNTIME = ImmutableUtils
			.makeImmutableNavigableSet(new String[] { "compile", "runtime", "test" });

	private MavenDependencyResolutionTaskOutputImpl resolutionOutput;

	/**
	 * For {@link Externalizable}.
	 */
	public MavenDependencyResolutionScopesOutputImpl() {
	}

	public MavenDependencyResolutionScopesOutputImpl(MavenDependencyResolutionTaskOutputImpl resolutionOutput) {
		this.resolutionOutput = resolutionOutput;
	}

	@Override
	public MavenDependencyResolutionTaskOutput get(String scope) {
		if (scope == null) {
			return new MavenDependencyResolutionTaskOutputImpl(resolutionOutput.getConfiguration(),
					Collections.emptySet());
		}
		Set<String> acceptedscopes;
		Set<ResolvedDependencyArtifact> artifacts = new LinkedHashSet<>();
		switch (scope) {
			case "Compilation": {
				acceptedscopes = SCOPES_COMPILE_PROVIDED;
				break;
			}
			case "Execution": {
				acceptedscopes = SCOPES_COMPILE_RUNTIME;
				break;
			}

			case "TestCompilation": {
				acceptedscopes = SCOPES_TEST_COMPILE_PROVIDED;
				break;
			}
			case "TestExecution": {
				acceptedscopes = SCOPES_TEST_COMPILE_RUNTIME;
				break;
			}

			default: {
				acceptedscopes = ObjectUtils.newHashSet(PATTERN_SPLIT_OR.split(scope));
				acceptedscopes.remove("");
				break;
			}
		}
		for (ResolvedDependencyArtifact art : resolutionOutput.getResolvedArtifacts()) {
			if (acceptedscopes.contains(art.getScope())) {
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
		MavenDependencyResolutionScopesOutputImpl other = (MavenDependencyResolutionScopesOutputImpl) obj;
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
