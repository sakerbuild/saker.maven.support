package saker.maven.support.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.file.content.ContentDescriptor;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.ExecutionProperty;

public class ArtifactContentDescriptorExecutionProperty implements ExecutionProperty<ContentDescriptor>, Externalizable {
	private static final long serialVersionUID = 1L;

	//object that uniquely identifies this execution property request
	private Object uniqueness;
	private SakerPath path;

	/**
	 * For {@link Externalizable}.
	 */
	public ArtifactContentDescriptorExecutionProperty() {
	}

	public ArtifactContentDescriptorExecutionProperty(Object uniqueness, SakerPath path) {
		this.uniqueness = uniqueness;
		this.path = path;
	}

	@Override
	public ContentDescriptor getCurrentValue(ExecutionContext executioncontext) {
		try {
			ContentDescriptor result = executioncontext
					.getContentDescriptor(LocalFileProvider.getInstance().getPathKey(path));
			return result;
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(uniqueness);
		out.writeObject(path);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		uniqueness = in.readObject();
		path = (SakerPath) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + ((uniqueness == null) ? 0 : uniqueness.hashCode());
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
		ArtifactContentDescriptorExecutionProperty other = (ArtifactContentDescriptorExecutionProperty) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (uniqueness == null) {
			if (other.uniqueness != null)
				return false;
		} else if (!uniqueness.equals(other.uniqueness))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[path=" + path + "]";
	}

}
