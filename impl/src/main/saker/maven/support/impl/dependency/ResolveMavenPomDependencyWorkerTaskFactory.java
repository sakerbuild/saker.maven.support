package saker.maven.support.impl.dependency;

import java.io.Externalizable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.file.SakerFile;
import saker.build.file.path.SakerPath;
import saker.build.task.CommonTaskContentDescriptors;
import saker.build.task.TaskContext;
import saker.maven.support.api.MavenOperationConfiguration;
import saker.maven.support.api.dependency.MavenDependencyResolutionTaskOutput;
import saker.maven.support.main.dependency.ResolveMavenDependencyTaskFactory;

public class ResolveMavenPomDependencyWorkerTaskFactory extends ResolveMavenDependencyWorkerTaskFactoryBase {
	private static final long serialVersionUID = 1L;

	private SakerPath pomPath;

	/**
	 * For {@link Externalizable}.
	 */
	public ResolveMavenPomDependencyWorkerTaskFactory() {
	}

	public ResolveMavenPomDependencyWorkerTaskFactory(MavenOperationConfiguration config, SakerPath pomPath) {
		super(config);
		this.pomPath = pomPath;
	}

	@Override
	public MavenDependencyResolutionTaskOutput run(TaskContext taskcontext) throws Exception {
		taskcontext.setStandardOutDisplayIdentifier(ResolveMavenDependencyTaskFactory.TASK_NAME);

		SakerFile pomfile = taskcontext.getTaskUtilities().resolveFileAtPath(pomPath);
		if (pomfile == null) {
			taskcontext.reportInputFileDependency(null, pomPath, CommonTaskContentDescriptors.IS_NOT_FILE);
			taskcontext.abortExecution(new FileNotFoundException("Pom not found: " + pomPath));
			return null;
		}
		taskcontext.getTaskUtilities().reportInputFileDependency(null, pomfile);
		return resolvePomDependencies(taskcontext, pomfile);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeObject(pomPath);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		pomPath = (SakerPath) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((pomPath == null) ? 0 : pomPath.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResolveMavenPomDependencyWorkerTaskFactory other = (ResolveMavenPomDependencyWorkerTaskFactory) obj;
		if (pomPath == null) {
			if (other.pomPath != null)
				return false;
		} else if (!pomPath.equals(other.pomPath))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (pomPath != null ? "pomPath=" + pomPath : "") + "]";
	}
}
