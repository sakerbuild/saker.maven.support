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