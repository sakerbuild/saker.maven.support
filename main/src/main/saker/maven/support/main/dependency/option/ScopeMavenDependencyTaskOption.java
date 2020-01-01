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
package saker.maven.support.main.dependency.option;

import java.util.Collection;

final class ScopeMavenDependencyTaskOption implements MavenDependencyTaskOption {
	private final String scope;

	ScopeMavenDependencyTaskOption(String scope) {
		this.scope = scope;
	}

	@Override
	public String getScope() {
		return scope;
	}

	@Override
	public Boolean getOptional() {
		return null;
	}

	@Override
	public Collection<ExclusionTaskOption> getExclusions() {
		return null;
	}
}