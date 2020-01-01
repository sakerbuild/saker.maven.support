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
package saker.maven.support.impl.dependency.option;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.Set;

import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;

public final class MavenDependencyOption implements Externalizable {
	private static final long serialVersionUID = 1L;

	private String scope;
	private Boolean optional;
	private Set<? extends ExclusionOption> exclusions;

	/**
	 * For {@link Externalizable}.
	 */
	public MavenDependencyOption() {
	}

	public MavenDependencyOption(String scope, Boolean optional, Set<? extends ExclusionOption> exclusions) {
		this.scope = scope;
		this.optional = optional;
		this.exclusions = exclusions == null ? Collections.emptySet()
				: ImmutableUtils.makeImmutableLinkedHashSet(exclusions);
	}

	public static MavenDependencyOption forScope(String scope) {
		return new MavenDependencyOption(scope, null, Collections.emptySet());
	}

	public String getScope() {
		return scope;
	}

	public Boolean getOptional() {
		return optional;
	}

	public Set<? extends ExclusionOption> getExclusions() {
		return exclusions;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(scope);
		out.writeObject(optional);
		SerialUtils.writeExternalCollection(out, exclusions);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		scope = (String) in.readObject();
		optional = (Boolean) in.readObject();
		exclusions = SerialUtils.readExternalImmutableLinkedHashSet(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((exclusions == null) ? 0 : exclusions.hashCode());
		result = prime * result + ((optional == null) ? 0 : optional.hashCode());
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
		MavenDependencyOption other = (MavenDependencyOption) obj;
		if (exclusions == null) {
			if (other.exclusions != null)
				return false;
		} else if (!exclusions.equals(other.exclusions))
			return false;
		if (optional == null) {
			if (other.optional != null)
				return false;
		} else if (!optional.equals(other.optional))
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
		return getClass().getSimpleName() + "[" + (scope != null ? "scope=" + scope + ", " : "")
				+ (optional != null ? "optional=" + optional + ", " : "")
				+ (exclusions != null ? "exclusions=" + exclusions : "") + "]";
	}

}
