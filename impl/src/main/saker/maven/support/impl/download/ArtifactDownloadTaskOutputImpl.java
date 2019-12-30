package saker.maven.support.impl.download;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import saker.build.task.utils.SimpleStructuredListTaskResult;
import saker.build.task.utils.StructuredListTaskResult;
import saker.build.task.utils.StructuredTaskResult;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.maven.support.api.ArtifactCoordinates;
import saker.maven.support.api.MavenOperationConfiguration;
import saker.maven.support.api.download.ArtifactDownloadTaskOutput;

public class ArtifactDownloadTaskOutputImpl implements ArtifactDownloadTaskOutput, Externalizable {
	private static final long serialVersionUID = 1L;

	private MavenOperationConfiguration configuration;
	//ArtifactDownloadWorkerTaskOutput values
	private Map<ArtifactCoordinates, StructuredTaskResult> coordinateResults;

	/**
	 * For {@link Externalizable}.
	 */
	public ArtifactDownloadTaskOutputImpl() {
	}

	public ArtifactDownloadTaskOutputImpl(MavenOperationConfiguration configuration,
			Map<ArtifactCoordinates, StructuredTaskResult> coordinateResults) {
		this.coordinateResults = coordinateResults;
		this.configuration = configuration;
	}

	@Override
	public MavenOperationConfiguration getConfiguration() {
		return configuration;
	}

	@Override
	public StructuredListTaskResult getArtifactPaths() {
		List<StructuredTaskResult> pathtaskresults = new ArrayList<>();
		for (StructuredTaskResult dltaskres : coordinateResults.values()) {
			pathtaskresults.add(new ArtifactDownloadWorkerTaskPathStructuredTaskResult(dltaskres));
		}
		return new SimpleStructuredListTaskResult(pathtaskresults);
	}

	@Override
	public StructuredListTaskResult getDownloadResults() {
		return new SimpleStructuredListTaskResult(ImmutableUtils.makeImmutableList(coordinateResults.values()));
	}

	@Override
	public Collection<ArtifactCoordinates> getCoordinates() {
		return ImmutableUtils.unmodifiableSet(coordinateResults.keySet());
	}

	@Override
	public StructuredTaskResult getDownloadResult(ArtifactCoordinates artifactcoordinate) {
		if (artifactcoordinate == null) {
			return null;
		}
		return coordinateResults.get(artifactcoordinate);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(configuration);
		SerialUtils.writeExternalMap(out, coordinateResults);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		configuration = (MavenOperationConfiguration) in.readObject();
		coordinateResults = SerialUtils.readExternalImmutableLinkedHashMap(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((configuration == null) ? 0 : configuration.hashCode());
		result = prime * result + ((coordinateResults == null) ? 0 : coordinateResults.hashCode());
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
		ArtifactDownloadTaskOutputImpl other = (ArtifactDownloadTaskOutputImpl) obj;
		if (configuration == null) {
			if (other.configuration != null)
				return false;
		} else if (!configuration.equals(other.configuration))
			return false;
		if (coordinateResults == null) {
			if (other.coordinateResults != null)
				return false;
		} else if (!coordinateResults.equals(other.coordinateResults))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + coordinateResults + "]";
	}

}
