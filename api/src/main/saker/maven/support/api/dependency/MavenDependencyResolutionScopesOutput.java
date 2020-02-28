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
package saker.maven.support.api.dependency;

/**
 * Interface for limiting the scope of a dependency resolution output.
 * <p>
 * Callers can use the {@link #get(String)} method to get the resolution output that contains only the specified scopes.
 * <p>
 * Clients shouldn't implement this interface.
 */
public interface MavenDependencyResolutionScopesOutput {
	/**
	 * Gets the dependency resolution output for the specified scopes.
	 * <p>
	 * The argument can be either the following:
	 * <ul>
	 * <li><code>"Compilation"</code>: for the scopes <code>compile</code> and <code>provided</code>.</li>
	 * <li><code>"Execution"</code>: for the scopes <code>compile</code> and <code>runtime</code>.</li>
	 * <li><code>"TestCompilation"</code>: for the scopes <code>test</code>, <code>compile</code> and
	 * <code>provided</code>.</li>
	 * <li><code>"TestExecution"</code>: for the scopes <code>test</code>, <code>compile</code> and
	 * <code>runtime</code>.</li>
	 * <li><code>"&lt;scope&gt;"</code>: a single Maven scope.</li>
	 * <li><code>"&lt;scope&gt;[|&lt;scope&gt;]*"</code>: multiple Maven scopes.</li>
	 * </ul>
	 * The returned resolution output object will only contain dependencies that have any of the specified scopes.
	 * 
	 * @param scope
	 *            The scope to filter for. If <code>null</code>, the result contains no artifacts.
	 * @return The filtered resolution output.
	 */
	public MavenDependencyResolutionTaskOutput get(String scope);
}
