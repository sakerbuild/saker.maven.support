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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Iterator;
import java.util.List;

import saker.build.task.TaskResultResolver;
import saker.build.task.utils.StructuredTaskResult;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;

public class RetrievalFailedStructuredTaskResult implements StructuredTaskResult, Externalizable {
	private static final long serialVersionUID = 1L;

	private String message;
	private List<? extends Throwable> causes;

	/**
	 * For {@link Externalizable}.
	 */
	public RetrievalFailedStructuredTaskResult() {
	}

	public RetrievalFailedStructuredTaskResult(String message, List<? extends Throwable> causes) {
		this.message = message;
		this.causes = causes;
	}

	@Override
	public Object toResult(TaskResultResolver results) {
		if (ObjectUtils.isNullOrEmpty(causes)) {
			throw new ArtifactRetrievalFailedException(message);
		}
		Iterator<? extends Throwable> it = causes.iterator();
		ArtifactRetrievalFailedException exc = new ArtifactRetrievalFailedException(message, it.next());
		while (it.hasNext()) {
			Throwable c = it.next();
			if (c == null) {
				continue;
			}
			exc.addSuppressed(c);
		}
		throw exc;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(message);
		SerialUtils.writeExternalCollection(out, causes);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		message = (String) in.readObject();
		causes = SerialUtils.readExternalImmutableList(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((causes == null) ? 0 : causes.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
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
		RetrievalFailedStructuredTaskResult other = (RetrievalFailedStructuredTaskResult) obj;
		if (causes == null) {
			if (other.causes != null)
				return false;
		} else if (!causes.equals(other.causes))
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (message != null ? "message=" + message + ", " : "")
				+ (causes != null ? "causes=" + causes : "") + "]";
	}
}
