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
package saker.maven.support.impl.dependency;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import saker.build.task.TaskContext;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.maven.support.api.ArtifactCoordinates;
import saker.maven.support.api.MavenOperationConfiguration;
import saker.maven.support.api.dependency.MavenDependencyResolutionTaskOutput;
import saker.maven.support.impl.ArtifactUtils;
import saker.maven.support.impl.MavenImplUtils;
import saker.maven.support.impl.dependency.option.ExclusionOption;
import saker.maven.support.impl.dependency.option.MavenDependencyOption;
import saker.maven.support.main.dependency.ResolveMavenDependencyTaskFactory;
import saker.maven.support.thirdparty.org.eclipse.aether.artifact.Artifact;
import saker.maven.support.thirdparty.org.eclipse.aether.graph.Dependency;
import saker.maven.support.thirdparty.org.eclipse.aether.graph.Exclusion;

public class ResolveMavenArtifactDependencyWorkerTaskFactory extends ResolveMavenDependencyWorkerTaskFactoryBase {
	private static final long serialVersionUID = 1L;

	private Map<? extends ArtifactCoordinates, ? extends MavenDependencyOption> coordinates;

	/**
	 * For {@link Externalizable}.
	 */
	public ResolveMavenArtifactDependencyWorkerTaskFactory() {
	}

	public ResolveMavenArtifactDependencyWorkerTaskFactory(
			Map<? extends ArtifactCoordinates, ? extends MavenDependencyOption> coordinates,
			MavenOperationConfiguration config) {
		super(config);
		this.coordinates = coordinates;
	}

	@Override
	public MavenDependencyResolutionTaskOutput run(TaskContext taskcontext) throws Exception {
		taskcontext.setStandardOutDisplayIdentifier(ResolveMavenDependencyTaskFactory.TASK_NAME);

		List<Dependency> collectdependencies = new ArrayList<>();
		for (Entry<? extends ArtifactCoordinates, ? extends MavenDependencyOption> entry : this.coordinates
				.entrySet()) {
			Artifact artifact = ArtifactUtils.toArtifact(entry.getKey());
			MavenDependencyOption depoption = entry.getValue();
			String scope = depoption.getScope();
			Set<Exclusion> depexclusions;
			Collection<? extends ExclusionOption> exclusions = depoption.getExclusions();
			if (!ObjectUtils.isNullOrEmpty(exclusions)) {
				depexclusions = new LinkedHashSet<>();
				for (ExclusionOption excloption : exclusions) {
					if (excloption == null) {
						continue;
					}
					depexclusions.add(MavenImplUtils.toExclusion(excloption));
				}
			} else {
				depexclusions = Collections.emptySet();
			}
			Dependency dep = new Dependency(artifact, scope, depoption.getOptional(), depexclusions);

			collectdependencies.add(dep);
		}
		return resolveDependencies(taskcontext, collectdependencies);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		SerialUtils.writeExternalMap(out, coordinates);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		coordinates = SerialUtils.readExternalImmutableLinkedHashMap(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((coordinates == null) ? 0 : coordinates.hashCode());
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
		ResolveMavenArtifactDependencyWorkerTaskFactory other = (ResolveMavenArtifactDependencyWorkerTaskFactory) obj;
		if (coordinates == null) {
			if (other.coordinates != null)
				return false;
		} else if (!coordinates.equals(other.coordinates))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + coordinates + "]";
	}
}
