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
package saker.maven.support.impl;

public class ArtifactRetrievalFailedException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ArtifactRetrievalFailedException() {
		super();
	}

	protected ArtifactRetrievalFailedException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ArtifactRetrievalFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ArtifactRetrievalFailedException(String message) {
		super(message);
	}

	public ArtifactRetrievalFailedException(Throwable cause) {
		super(cause);
	}

}
