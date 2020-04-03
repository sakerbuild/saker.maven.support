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
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import saker.build.file.SakerFile;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.task.identifier.TaskIdentifier;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.maven.support.api.ArtifactCoordinates;
import saker.maven.support.api.MavenOperationConfiguration;
import saker.maven.support.api.dependency.MavenDependencyResolutionTaskOutput;
import saker.maven.support.api.dependency.ResolvedDependencyArtifact;
import saker.maven.support.impl.ArtifactUtils;
import saker.maven.support.impl.BugFixDefaultModelBuilderFactory;
import saker.maven.support.impl.MavenImplUtils;
import saker.maven.support.impl.SakerFileModelSource;
import saker.maven.support.impl.dependency.option.ExclusionOption;
import saker.maven.support.impl.dependency.option.MavenDependencyOption;
import saker.maven.support.thirdparty.org.apache.maven.model.Model;
import saker.maven.support.thirdparty.org.apache.maven.model.building.DefaultModelBuildingRequest;
import saker.maven.support.thirdparty.org.apache.maven.model.building.ModelBuilder;
import saker.maven.support.thirdparty.org.apache.maven.model.building.ModelBuildingException;
import saker.maven.support.thirdparty.org.apache.maven.model.building.ModelBuildingRequest;
import saker.maven.support.thirdparty.org.apache.maven.model.building.ModelBuildingResult;
import saker.maven.support.thirdparty.org.apache.maven.model.building.ModelProblemCollector;
import saker.maven.support.thirdparty.org.apache.maven.model.locator.DefaultModelLocator;
import saker.maven.support.thirdparty.org.apache.maven.model.locator.ModelLocator;
import saker.maven.support.thirdparty.org.apache.maven.model.validation.ModelValidator;
import saker.maven.support.thirdparty.org.eclipse.aether.DefaultRepositorySystemSession;
import saker.maven.support.thirdparty.org.eclipse.aether.RepositorySystem;
import saker.maven.support.thirdparty.org.eclipse.aether.artifact.Artifact;
import saker.maven.support.thirdparty.org.eclipse.aether.artifact.DefaultArtifact;
import saker.maven.support.thirdparty.org.eclipse.aether.collection.CollectRequest;
import saker.maven.support.thirdparty.org.eclipse.aether.collection.CollectResult;
import saker.maven.support.thirdparty.org.eclipse.aether.graph.Dependency;
import saker.maven.support.thirdparty.org.eclipse.aether.graph.DependencyNode;
import saker.maven.support.thirdparty.org.eclipse.aether.graph.DependencyVisitor;
import saker.maven.support.thirdparty.org.eclipse.aether.graph.Exclusion;
import saker.maven.support.thirdparty.org.eclipse.aether.impl.DefaultServiceLocator;
import saker.maven.support.thirdparty.org.eclipse.aether.repository.LocalRepository;
import saker.maven.support.thirdparty.org.eclipse.aether.repository.RemoteRepository;
import saker.maven.support.thirdparty.org.eclipse.aether.resolution.ArtifactRequest;
import saker.maven.support.thirdparty.org.eclipse.aether.resolution.ArtifactResult;

public abstract class ResolveMavenDependencyWorkerTaskFactoryBase
		implements TaskFactory<MavenDependencyResolutionTaskOutput>, Task<MavenDependencyResolutionTaskOutput>,
		Externalizable, TaskIdentifier {
	private static final long serialVersionUID = 1L;

	private MavenOperationConfiguration operationConfiguration;

	/**
	 * For {@link Externalizable}.
	 */
	public ResolveMavenDependencyWorkerTaskFactoryBase() {
	}

	public ResolveMavenDependencyWorkerTaskFactoryBase(MavenOperationConfiguration config) {
		Objects.requireNonNull(config, "maven operation configuration");
		this.operationConfiguration = config;
	}

	@Override
	public Task<? extends MavenDependencyResolutionTaskOutput> createTask(ExecutionContext executioncontext) {
		return this;
	}

	private interface LockedRepositoryOperationSupplier<T> {
		public T get(List<RemoteRepository> repositories, RepositorySystem reposystem,
				DefaultRepositorySystemSession reposession) throws Exception;
	}

	protected MavenDependencyResolutionTaskOutput resolveArtifactDependencies(TaskContext taskcontext,
			Map<? extends ArtifactCoordinates, ? extends MavenDependencyOption> coordinates) throws Exception {
		return resolveDependencies(taskcontext, (repositories, reposystem, reposession) -> {
			List<Dependency> collectdependencies = new ArrayList<>();
			Map<ArtifactCoordinates, ArtifactRequest> pomrequests = getNoExtensionPomArtifactRequests(coordinates,
					repositories);
			Map<ArtifactCoordinates, ArtifactResult> pomresults = null;
			if (!ObjectUtils.isNullOrEmpty(pomrequests)) {
				List<ArtifactResult> resolutionresults = reposystem.resolveArtifacts(reposession, pomrequests.values());
				pomresults = new HashMap<>();
				Map<ArtifactRequest, ArtifactCoordinates> requestpoms = new HashMap<>();
				for (Entry<ArtifactCoordinates, ArtifactRequest> entry : pomrequests.entrySet()) {
					requestpoms.put(entry.getValue(), entry.getKey());
				}
				for (ArtifactResult res : resolutionresults) {
					ArtifactRequest request = res.getRequest();
					pomresults.put(requestpoms.get(request), res);
				}
			}
			for (Entry<? extends ArtifactCoordinates, ? extends MavenDependencyOption> entry : coordinates.entrySet()) {
				ArtifactCoordinates acoords = entry.getKey();
				if (ObjectUtils.isNullOrEmpty(acoords.getExtension())) {
					ArtifactResult pomresolutionresult = pomresults.get(acoords);

					//retrieve the packaging from the model as the extension
					DefaultModelBuildingRequest buildrequest = createModelBuildingRequest(repositories, reposystem,
							reposession);
					File pomfile = pomresolutionresult.getArtifact().getFile();
					buildrequest.setPomFile(pomfile);
					Model model = buildSimpleModel(buildrequest);
					acoords = new ArtifactCoordinates(acoords.getGroupId(), acoords.getArtifactId(),
							acoords.getClassifier(), model.getPackaging(), acoords.getVersion());
				}
				Artifact artifact = ArtifactUtils.toArtifact(acoords);
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
			return dependenciesToCollectRequest(collectdependencies, repositories);
		});
	}

	private static Map<ArtifactCoordinates, ArtifactRequest> getNoExtensionPomArtifactRequests(
			Map<? extends ArtifactCoordinates, ? extends MavenDependencyOption> coordinates,
			List<RemoteRepository> repositories) {
		Map<ArtifactCoordinates, ArtifactRequest> pomrequest = new HashMap<>();
		for (Entry<? extends ArtifactCoordinates, ? extends MavenDependencyOption> entry : coordinates.entrySet()) {
			ArtifactCoordinates acoords = entry.getKey();
			if (ObjectUtils.isNullOrEmpty(acoords.getExtension())) {
				ArtifactRequest artrequest = new ArtifactRequest(new DefaultArtifact(acoords.getGroupId(),
						acoords.getArtifactId(), "", "pom", acoords.getVersion()), repositories, null);
				pomrequest.put(acoords, artrequest);
			}
		}
		return pomrequest;
	}

	protected MavenDependencyResolutionTaskOutput resolvePomDependencies(TaskContext taskcontext, SakerFile pomfile)
			throws Exception {
		return resolveDependencies(taskcontext, (repositories, reposystem, reposession) -> {
			DefaultModelBuildingRequest modelbuildrequest = createModelBuildingRequest(repositories, reposystem,
					reposession).setModelSource(new SakerFileModelSource(taskcontext, pomfile));

			Model model = buildSimpleModel(modelbuildrequest);

			List<saker.maven.support.thirdparty.org.apache.maven.model.Dependency> modeldependencies = model
					.getDependencies();

			List<Dependency> collectdependencies = new ArrayList<>();

			for (saker.maven.support.thirdparty.org.apache.maven.model.Dependency modeldep : modeldependencies) {
				DefaultArtifact artifact = new DefaultArtifact(modeldep.getGroupId(), modeldep.getArtifactId(),
						modeldep.getType(), modeldep.getVersion());
				Set<saker.maven.support.thirdparty.org.eclipse.aether.graph.Exclusion> collectexclusions;
				List<saker.maven.support.thirdparty.org.apache.maven.model.Exclusion> modelexclusions = modeldep
						.getExclusions();
				if (!ObjectUtils.isNullOrEmpty(modelexclusions)) {
					collectexclusions = new LinkedHashSet<>();
					for (saker.maven.support.thirdparty.org.apache.maven.model.Exclusion ex : modelexclusions) {
						collectexclusions.add(new Exclusion(ex.getGroupId(), ex.getArtifactId(), "*", "*"));
					}
				} else {
					collectexclusions = Collections.emptySet();
				}

				Dependency collectdep = new Dependency(artifact, modeldep.getScope(),
						Boolean.parseBoolean(modeldep.getOptional()), collectexclusions);
				collectdependencies.add(collectdep);
			}
			return dependenciesToCollectRequest(collectdependencies, repositories);
		});

	}

	private static DefaultModelBuildingRequest createModelBuildingRequest(List<RemoteRepository> repositories,
			RepositorySystem reposystem, DefaultRepositorySystemSession reposession) {
		return new DefaultModelBuildingRequest()
				.setModelResolver(new ReimplementedDefaultModelResolver(repositories, reposystem, reposession));
	}

	private static Model buildSimpleModel(ModelBuildingRequest modelbuildrequest) throws ModelBuildingException {
		ModelBuilder modelbuilder = new BugFixDefaultModelBuilderFactory() {
			@Override
			protected ModelLocator newModelLocator() {
				return new DefaultModelLocator() {
					@Override
					public File locatePom(File projectDirectory) {
						throw new UnsupportedOperationException(
								"Internal error: ModelLocator.locatePom(File) is unsupported.");
					}
				};
			}

			@Override
			protected ModelValidator newModelValidator() {
				return new NonDependencyClearingModelValidator();
			}

		}.newInstance();
		ModelBuildingResult modelbuildresult = modelbuilder.build(modelbuildrequest);
		Model model = modelbuildresult.getEffectiveModel();
		return model;
	}

	//suppress the unused FileLock warning
	@SuppressWarnings("try")
	protected MavenDependencyResolutionTaskOutput resolveDependencies(TaskContext taskcontext,
			LockedRepositoryOperationSupplier<CollectRequest> collectrequestsupplier) throws Exception {
		MavenOperationConfiguration config = operationConfiguration;

		List<RemoteRepository> repositories = MavenImplUtils.createRemoteRepositories(config);

		SakerPath repositorybasedir = MavenImplUtils.getRepositoryBaseDirectoryDefaulted(taskcontext, config);

		SakerPath lockfilepath = MavenImplUtils.getAccessLockFilePathInRepository(repositorybasedir);
		Path lockfilelocalpath = LocalFileProvider.toRealPath(lockfilepath);

		LocalFileProvider localfp = LocalFileProvider.getInstance();
		localfp.createDirectories(lockfilelocalpath.getParent());

		DefaultServiceLocator serviceLocator = MavenImplUtils.getDefaultServiceLocator();

		RepositorySystem reposystem = serviceLocator.getService(RepositorySystem.class);

		DefaultRepositorySystemSession reposession = MavenImplUtils.createNewSession(taskcontext, config);

		LocalRepository localrepository = new LocalRepository(repositorybasedir.toString());
		reposession.setLocalRepositoryManager(reposystem.newLocalRepositoryManager(reposession, localrepository));

		reposession.setReadOnly();

		Collection<ResolvedDependencyArtifact> entries = new LinkedHashSet<>();

		synchronized (MavenImplUtils.getLocalRepositoryAccessSyncLock(lockfilepath)) {
			try (FileChannel lockchannel = FileChannel.open(lockfilelocalpath, StandardOpenOption.CREATE,
					StandardOpenOption.WRITE);
					FileLock lock = lockchannel.lock(0, Long.MAX_VALUE, false)) {
				CollectRequest collectrequest = collectrequestsupplier.get(repositories, reposystem, reposession);
				CollectResult collectdeps = reposystem.collectDependencies(reposession, collectrequest);
				DependencyNode rootdepnode = collectdeps.getRoot();
				rootdepnode.accept(new DependencyVisitor() {
					@Override
					public boolean visitLeave(DependencyNode node) {
						return true;
					}

					@Override
					public boolean visitEnter(DependencyNode node) {
						Dependency dependency = node.getDependency();
						if (dependency == null) {
							//may be null for the root node
							return true;
						}
						Artifact artifact = node.getArtifact();
						if (artifact != null) {
							entries.add(new ResolvedDependencyArtifactImpl(
									ArtifactUtils.toArtifactCoordinates(artifact), dependency.getScope(), config));
						}
						return true;
					}
				});
			}
		}

		MavenDependencyResolutionTaskOutputImpl result = new MavenDependencyResolutionTaskOutputImpl(config, entries);
		return result;
	}

	private static CollectRequest dependenciesToCollectRequest(List<Dependency> collectdependencies,
			List<RemoteRepository> repositories) {
		CollectRequest collectrequest = new CollectRequest();
		collectrequest.setRoot(null);
		collectrequest.setDependencies(collectdependencies);
		collectrequest.setRepositories(repositories);
		return collectrequest;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(operationConfiguration);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		operationConfiguration = (MavenOperationConfiguration) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((operationConfiguration == null) ? 0 : operationConfiguration.hashCode());
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
		ResolveMavenDependencyWorkerTaskFactoryBase other = (ResolveMavenDependencyWorkerTaskFactoryBase) obj;
		if (operationConfiguration == null) {
			if (other.operationConfiguration != null)
				return false;
		} else if (!operationConfiguration.equals(other.operationConfiguration))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[]";
	}

	protected static final class NonDependencyClearingModelValidator implements ModelValidator {
		@Override
		public void validateRawModel(Model model, ModelBuildingRequest arg1, ModelProblemCollector arg2) {
			clearNonDependenciesFromModel(model);
		}

		@Override
		public void validateEffectiveModel(Model model, ModelBuildingRequest arg1, ModelProblemCollector arg2) {
			clearNonDependenciesFromModel(model);
		}

		private static void clearNonDependenciesFromModel(Model model) {
			//we dont care about these things. dont let other validations and maven features bother us

			//don't clear parent!!!

			model.getContributors().clear();
			model.getDevelopers().clear();
			model.getLicenses().clear();
			model.getMailingLists().clear();
			model.getPluginRepositories().clear();
			model.getProfiles().clear();
			model.getRepositories().clear();
			model.getModules().clear();
			model.setCiManagement(null);
			model.setDependencyManagement(null);
			model.setDescription(null);
			model.setDistributionManagement(null);
			model.setIssueManagement(null);
			model.setOrganization(null);
			model.setScm(null);
			model.setReporting(null);
			model.setProperties(null);
		}
	}
}
