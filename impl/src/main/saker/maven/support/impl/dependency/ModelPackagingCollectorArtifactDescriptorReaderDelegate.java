package saker.maven.support.impl.dependency;

import java.util.LinkedHashMap;
import java.util.Map;

import saker.maven.support.thirdparty.org.apache.maven.model.Model;
import saker.maven.support.thirdparty.org.apache.maven.repository.internal.ArtifactDescriptorReaderDelegate;
import saker.maven.support.thirdparty.org.eclipse.aether.RepositorySystemSession;
import saker.maven.support.thirdparty.org.eclipse.aether.artifact.Artifact;
import saker.maven.support.thirdparty.org.eclipse.aether.resolution.ArtifactDescriptorResult;

public final class ModelPackagingCollectorArtifactDescriptorReaderDelegate extends ArtifactDescriptorReaderDelegate {
	private Map<String, String> modelIdPackagings = new LinkedHashMap<>();

	@Override
	public void populateResult(RepositorySystemSession session, ArtifactDescriptorResult result, Model model) {
		String modelidstr = model.getGroupId() + ":" + model.getArtifactId() + ":" + model.getVersion();
		modelIdPackagings.put(modelidstr, model.getPackaging());
		super.populateResult(session, result, model);
	}

	public Map<String, String> getModelIdPackagings() {
		return modelIdPackagings;
	}

	public String getPackaging(Artifact artifact) {
		String modelidstr = artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion();
		return modelIdPackagings.get(modelidstr);
	}
}