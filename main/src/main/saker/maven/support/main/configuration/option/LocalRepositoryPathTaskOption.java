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
package saker.maven.support.main.configuration.option;

import saker.build.exception.InvalidPathFormatException;
import saker.build.file.path.SakerPath;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.std.api.file.location.LocalFileLocation;

@NestInformation("Local file system path to a Maven repository.")
public class LocalRepositoryPathTaskOption {
	private SakerPath path;

	public LocalRepositoryPathTaskOption(SakerPath path) {
		if (!path.isAbsolute()) {
			throw new InvalidPathFormatException("Local Maven repository path must be absolute: " + path);
		}
		this.path = path;
	}

	public SakerPath getPath() {
		return path;
	}

	public static LocalRepositoryPathTaskOption valueOf(String input) {
		return valueOf(SakerPath.valueOf(input));
	}

	public static LocalRepositoryPathTaskOption valueOf(SakerPath path) {
		return new LocalRepositoryPathTaskOption(path);
	}

	public static LocalRepositoryPathTaskOption valueOf(LocalFileLocation localfile) {
		return new LocalRepositoryPathTaskOption(localfile.getLocalPath());
	}
}
