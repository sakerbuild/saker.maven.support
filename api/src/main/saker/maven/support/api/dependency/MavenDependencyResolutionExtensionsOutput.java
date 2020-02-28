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
 * Interface for filtering out specific extensions of a depdendency resolution output.
 * <p>
 * Callers can use the {@link #get(String)} method to get the resolution output that contains artifacts only with the
 * specified extension(s).
 * <p>
 * Clients shouldn't implement this interface.
 * 
 * @since saker.maven.support 0.8.4
 */
public interface MavenDependencyResolutionExtensionsOutput {
	/**
	 * Gets the dependency resolution output for the specified extension(s).
	 * <p>
	 * The argument can be specified as a single extension string, or by passing multiple extensions separated by the
	 * <code>'|'</code> character.
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * MavenDependencyResolutionExtensionsOutput ext;
	 * // filter out only jar artifacts
	 * ext.get("jar");
	 * // filter out only aar and zip artifacts 
	 * ext.get("aar|zip");
	 * </pre>
	 * 
	 * @param extension
	 *            The extensions.
	 * @return The filtered resolution output.
	 */
	public MavenDependencyResolutionTaskOutput get(String extension);
}
