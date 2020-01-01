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
package saker.maven.support.impl.download;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.task.TaskResultResolver;
import saker.build.task.utils.StructuredTaskResult;
import saker.maven.support.api.download.ArtifactDownloadWorkerTaskOutput;

public class ArtifactDownloadWorkerTaskPathStructuredTaskResult implements StructuredTaskResult, Externalizable {
	private static final long serialVersionUID = 1L;

	private StructuredTaskResult downloadWorkerTaskResult;

	/**
	 * For {@link Externalizable}.
	 */
	public ArtifactDownloadWorkerTaskPathStructuredTaskResult() {
	}

	public ArtifactDownloadWorkerTaskPathStructuredTaskResult(StructuredTaskResult downloadWorkerTaskResult) {
		this.downloadWorkerTaskResult = downloadWorkerTaskResult;
	}

	@Override
	public Object toResult(TaskResultResolver results) {
		ArtifactDownloadWorkerTaskOutput out = (ArtifactDownloadWorkerTaskOutput) downloadWorkerTaskResult
				.toResult(results);
		return out.getPath();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(downloadWorkerTaskResult);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		downloadWorkerTaskResult = (StructuredTaskResult) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((downloadWorkerTaskResult == null) ? 0 : downloadWorkerTaskResult.hashCode());
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
		ArtifactDownloadWorkerTaskPathStructuredTaskResult other = (ArtifactDownloadWorkerTaskPathStructuredTaskResult) obj;
		if (downloadWorkerTaskResult == null) {
			if (other.downloadWorkerTaskResult != null)
				return false;
		} else if (!downloadWorkerTaskResult.equals(other.downloadWorkerTaskResult))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + downloadWorkerTaskResult + "]";
	}

}