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

import java.io.File;
import java.util.List;

import saker.maven.support.thirdparty.org.apache.maven.model.Parent;
import saker.maven.support.thirdparty.org.apache.maven.model.Repository;
import saker.maven.support.thirdparty.org.apache.maven.model.building.FileModelSource;
import saker.maven.support.thirdparty.org.apache.maven.model.resolution.InvalidRepositoryException;
import saker.maven.support.thirdparty.org.apache.maven.model.resolution.ModelResolver;
import saker.maven.support.thirdparty.org.apache.maven.model.resolution.UnresolvableModelException;
import saker.maven.support.thirdparty.org.eclipse.aether.DefaultRepositorySystemSession;
import saker.maven.support.thirdparty.org.eclipse.aether.RepositorySystem;
import saker.maven.support.thirdparty.org.eclipse.aether.artifact.Artifact;
import saker.maven.support.thirdparty.org.eclipse.aether.artifact.DefaultArtifact;
import saker.maven.support.thirdparty.org.eclipse.aether.repository.RemoteRepository;
import saker.maven.support.thirdparty.org.eclipse.aether.resolution.ArtifactRequest;
import saker.maven.support.thirdparty.org.eclipse.aether.resolution.ArtifactResolutionException;
import saker.maven.support.thirdparty.org.eclipse.aether.resolution.VersionRangeRequest;
import saker.maven.support.thirdparty.org.eclipse.aether.resolution.VersionRangeResolutionException;
import saker.maven.support.thirdparty.org.eclipse.aether.resolution.VersionRangeResult;

@SuppressWarnings("deprecation")
final class ReimplementedDefaultModelResolver implements ModelResolver {
	//based on DefaultModelResolver

	private final List<RemoteRepository> repositories;
	private final RepositorySystem reposystem;
	private final DefaultRepositorySystemSession reposession;

	ReimplementedDefaultModelResolver(List<RemoteRepository> repositories, RepositorySystem reposystem,
			DefaultRepositorySystemSession reposession) {
		this.repositories = repositories;
		this.reposystem = reposystem;
		this.reposession = reposession;
	}

	@Override
	//fully qualify return type to avoid deprecation warning on import
	public saker.maven.support.thirdparty.org.apache.maven.model.building.ModelSource resolveModel(String groupId,
			String artifactId, String version) throws UnresolvableModelException {
		Artifact pomArtifact = new DefaultArtifact(groupId, artifactId, "", "pom", version);

		try {
			String context = null;
			ArtifactRequest request = new ArtifactRequest(pomArtifact, repositories, context);
			//XXX trace?
//								request.setTrace(trace);
			pomArtifact = reposystem.resolveArtifact(reposession, request).getArtifact();
		} catch (ArtifactResolutionException e) {
			throw new UnresolvableModelException(e.getMessage(), groupId, artifactId, version, e);
		}

		File pomFile = pomArtifact.getFile();

		return new FileModelSource(pomFile);
	}

	@Override
	//fully qualify return type to avoid deprecation warning on import
	public saker.maven.support.thirdparty.org.apache.maven.model.building.ModelSource resolveModel(
			saker.maven.support.thirdparty.org.apache.maven.model.Dependency arg0) throws UnresolvableModelException {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	//fully qualify return type to avoid deprecation warning on import
	public saker.maven.support.thirdparty.org.apache.maven.model.building.ModelSource resolveModel(Parent parent)
			throws UnresolvableModelException {
		try {
			final Artifact artifact = new DefaultArtifact(parent.getGroupId(), parent.getArtifactId(), "", "pom",
					parent.getVersion());

			String context = null;
			final VersionRangeRequest versionRangeRequest = new VersionRangeRequest(artifact, repositories, context);
			//XXX trace?
//								versionRangeRequest.setTrace(trace);

			final VersionRangeResult versionRangeResult = reposystem.resolveVersionRange(reposession,
					versionRangeRequest);

			if (versionRangeResult.getHighestVersion() == null) {
				throw new UnresolvableModelException(String
						.format("No versions matched the requested parent version range '%s'", parent.getVersion()),
						parent.getGroupId(), parent.getArtifactId(), parent.getVersion());

			}

			if (versionRangeResult.getVersionConstraint() != null
					&& versionRangeResult.getVersionConstraint().getRange() != null
					&& versionRangeResult.getVersionConstraint().getRange().getUpperBound() == null) {
				// Message below is checked for in the MNG-2199 core IT.
				throw new UnresolvableModelException(
						String.format("The requested parent version range '%s' does not specify an upper bound",
								parent.getVersion()),
						parent.getGroupId(), parent.getArtifactId(), parent.getVersion());

			}

			parent.setVersion(versionRangeResult.getHighestVersion().toString());

			return resolveModel(parent.getGroupId(), parent.getArtifactId(), parent.getVersion());
		} catch (final VersionRangeResolutionException e) {
			throw new UnresolvableModelException(e.getMessage(), parent.getGroupId(), parent.getArtifactId(),
					parent.getVersion(), e);

		}
	}

	@Override
	public ModelResolver newCopy() {
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public void addRepository(Repository repository, boolean replace) throws InvalidRepositoryException {
		if (reposession.isIgnoreArtifactDescriptorRepositories()) {
			return;
		}
		throw new UnsupportedOperationException("not implemented");
	}

	@Override
	public void addRepository(Repository repository) throws InvalidRepositoryException {
		if (reposession.isIgnoreArtifactDescriptorRepositories()) {
			return;
		}
		throw new UnsupportedOperationException("not implemented");
	}
}