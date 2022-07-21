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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import saker.build.file.SakerFile;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.runtime.execution.SakerLog;
import saker.build.task.TaskContext;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.trace.BuildTrace;
import saker.maven.support.api.ArtifactCoordinates;
import saker.maven.support.api.MavenOperationConfiguration;
import saker.maven.support.api.MavenOperationConfiguration.AccountAuthenticationConfiguration;
import saker.maven.support.api.MavenOperationConfiguration.AuthenticationConfiguration;
import saker.maven.support.api.MavenOperationConfiguration.PrivateKeyAuthenticationConfiguration;
import saker.maven.support.api.MavenOperationConfiguration.RepositoryConfiguration;
import saker.maven.support.api.MavenOperationConfiguration.RepositoryPolicyConfiguration;
import saker.maven.support.api.MavenUtils;
import saker.maven.support.impl.dependency.ModelPackagingCollectorArtifactDescriptorReaderDelegate;
import saker.maven.support.impl.dependency.option.ExclusionOption;
import saker.maven.support.thirdparty.org.apache.maven.model.Model;
import saker.maven.support.thirdparty.org.apache.maven.model.building.DefaultModelBuildingRequest;
import saker.maven.support.thirdparty.org.apache.maven.model.building.ModelBuilder;
import saker.maven.support.thirdparty.org.apache.maven.model.building.ModelBuildingException;
import saker.maven.support.thirdparty.org.apache.maven.model.building.ModelBuildingRequest;
import saker.maven.support.thirdparty.org.apache.maven.model.building.ModelBuildingResult;
import saker.maven.support.thirdparty.org.apache.maven.model.building.ModelProblemCollector;
import saker.maven.support.thirdparty.org.apache.maven.model.building.ModelSource2;
import saker.maven.support.thirdparty.org.apache.maven.model.locator.DefaultModelLocator;
import saker.maven.support.thirdparty.org.apache.maven.model.locator.ModelLocator;
import saker.maven.support.thirdparty.org.apache.maven.model.validation.ModelValidator;
import saker.maven.support.thirdparty.org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import saker.maven.support.thirdparty.org.apache.maven.wagon.ConnectionException;
import saker.maven.support.thirdparty.org.apache.maven.wagon.Wagon;
import saker.maven.support.thirdparty.org.apache.maven.wagon.providers.http.HttpWagon;
import saker.maven.support.thirdparty.org.eclipse.aether.AbstractRepositoryListener;
import saker.maven.support.thirdparty.org.eclipse.aether.DefaultRepositorySystemSession;
import saker.maven.support.thirdparty.org.eclipse.aether.RepositoryEvent;
import saker.maven.support.thirdparty.org.eclipse.aether.RepositorySystemSession;
import saker.maven.support.thirdparty.org.eclipse.aether.artifact.Artifact;
import saker.maven.support.thirdparty.org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import saker.maven.support.thirdparty.org.eclipse.aether.graph.Exclusion;
import saker.maven.support.thirdparty.org.eclipse.aether.impl.DefaultServiceLocator;
import saker.maven.support.thirdparty.org.eclipse.aether.internal.impl.DefaultChecksumPolicyProvider;
import saker.maven.support.thirdparty.org.eclipse.aether.repository.RemoteRepository;
import saker.maven.support.thirdparty.org.eclipse.aether.repository.RepositoryPolicy;
import saker.maven.support.thirdparty.org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import saker.maven.support.thirdparty.org.eclipse.aether.spi.connector.checksum.ChecksumPolicy;
import saker.maven.support.thirdparty.org.eclipse.aether.spi.connector.checksum.ChecksumPolicyProvider;
import saker.maven.support.thirdparty.org.eclipse.aether.spi.connector.transport.TransporterFactory;
import saker.maven.support.thirdparty.org.eclipse.aether.spi.log.LoggerFactory;
import saker.maven.support.thirdparty.org.eclipse.aether.transfer.ChecksumFailureException;
import saker.maven.support.thirdparty.org.eclipse.aether.transfer.TransferResource;
import saker.maven.support.thirdparty.org.eclipse.aether.transport.file.FileTransporterFactory;
import saker.maven.support.thirdparty.org.eclipse.aether.transport.wagon.WagonProvider;
import saker.maven.support.thirdparty.org.eclipse.aether.transport.wagon.WagonTransporterFactory;
import saker.maven.support.thirdparty.org.eclipse.aether.util.repository.AuthenticationBuilder;

public class MavenImplUtils {
	public static final String DEFAULT_CHECKSUM_POLICY = RepositoryPolicy.CHECKSUM_POLICY_WARN;
	public static final String DEFAULT_UPDATE_POLICY = RepositoryPolicy.UPDATE_POLICY_DAILY;

	private MavenImplUtils() {
		throw new UnsupportedOperationException();
	}

	public static SakerPath getAccessLockFilePathInRepository(SakerPath repositorybasedir) {
		return repositorybasedir.resolve("saker.m2.repository.lock");
	}

	public static String getLocalRepositoryAccessSyncLock(SakerPath lockfilepath) {
		//lock on a VM common object to avoid overlapped exceptions
		return ("maven.repository.lock:" + lockfilepath).toLowerCase(Locale.ENGLISH).intern();
	}

	public static RemoteRepository getMavenCentralRemoteRepository() {
		//disable snapshots as in the super pom
		return new RemoteRepository.Builder("central", "default", MavenUtils.MAVEN_CENTRAL_REPOSITORY_URL)
				.setSnapshotPolicy(new RepositoryPolicy(false, null, null)).build();
	}

	public static SakerPath getRepositoryBaseDirectoryDefaulted(TaskContext taskcontext,
			MavenOperationConfiguration config) {
		SakerPath result = config.getLocalRepositoryPath();
		if (result != null) {
			return result;
		}
		return MavenUtils.getDefaultMavenLocalRepositoryLocation(taskcontext);
	}

	public static void reportConfgurationBuildTrace(MavenOperationConfiguration config) {
		if (saker.build.meta.Versions.VERSION_FULL_COMPOUND < 8_006) {
			return;
		}
		BuildTrace.runWithBuildTrace(() -> {
			reportConfgurationBuildTraceWithBuildTrace(config);
		});
	}

	public static void reportConfgurationBuildTraceWithBuildTrace(MavenOperationConfiguration config) {
		//use exceptions to signal configuration errors
		if (config == null) {
			BuildTrace.setValues(
					Collections.singletonMap("Maven configuration",
							new NullPointerException("No Maven configuration was specified.")),
					BuildTrace.VALUE_CATEGORY_TASK);
			return;
		}

		LinkedHashMap<Object, Object> props = new LinkedHashMap<>();
		SakerPath localrepopath = config.getLocalRepositoryPath();
		if (localrepopath == null) {
			props.put("Repository local path",
					new NullPointerException("No Maven local repository path was specified."));
		} else {
			props.put("Repository local path", localrepopath.toString());
		}
		Set<? extends RepositoryConfiguration> repos = config.getRepositories();
		if (repos == null) {
			props.put("Remote repositories",
					new NullPointerException("Missing Maven remote repositories configuration."));
		} else {
			Map<String, Object> reposlist = new LinkedHashMap<>();
			for (RepositoryConfiguration repo : repos) {
				String id = repo.getId();
				String url = repo.getUrl();
				String title;
				if (id == null) {
					title = url;
				} else {
					title = id + "\t" + url;
				}
				Map<Object, Object> repoprops = createRepositoryConfigurationBuildTrace(repo);
				if (reposlist.putIfAbsent(title, repoprops) != null) {
					//already present with same title?! add with modified title
					int i = 2;
					while (true) {
						String ntitle = "(" + i + ") " + title;
						if (reposlist.putIfAbsent(ntitle, repoprops) == null) {
							break;
						}
						++i;
					}

				}
			}
			props.put("Remote repositories", reposlist);
		}
		BuildTrace.setValues(Collections.singletonMap("Maven configuration", props), BuildTrace.VALUE_CATEGORY_TASK);
	}

	public static Map<Object, Object> createRepositoryConfigurationBuildTrace(RepositoryConfiguration repo) {
		LinkedHashMap<Object, Object> repoprops = new LinkedHashMap<>();
		repoprops.put("Layout", repo.getLayout());
		addRepositoryPolicyBuildTrace(repo.getSnapshotPolicy(), "Snapshot policy", repoprops);
		addRepositoryPolicyBuildTrace(repo.getReleasePolicy(), "Release policy", repoprops);
		addRepositoryAuthenticationBuildTrace(repo.getAuthentication(), repoprops);
		return repoprops;
	}

	private static void addRepositoryAuthenticationBuildTrace(AuthenticationConfiguration authentication,
			LinkedHashMap<Object, Object> repoprops) {
		if (authentication == null) {
			return;
		}
		authentication.accept(new AuthenticationConfiguration.Visitor() {
			@Override
			public void visit(PrivateKeyAuthenticationConfiguration config) {
				repoprops.put("Authentication", "Private key");
			}

			@Override
			public void visit(AccountAuthenticationConfiguration config) {
				repoprops.put("Authentication", "Username + password");
			}
		});
	}

	private static void addRepositoryPolicyBuildTrace(RepositoryPolicyConfiguration policy, String name,
			LinkedHashMap<Object, Object> repoprops) {
		if (policy == null) {
			repoprops.put(name, "default");
			return;
		}
		if (!policy.isEnabled()) {
			repoprops.put(name, "disabled");
			return;
		}
		LinkedHashMap<Object, Object> props = new LinkedHashMap<>();
		props.put("Update policy", policy.getUpdatePolicy());
		props.put("Checksum policy", policy.getChecksumPolicy());
		repoprops.put(name, props);
	}

	public static DefaultServiceLocator getDefaultServiceLocator() {
		DefaultServiceLocator serviceLocator = MavenRepositorySystemUtils.newServiceLocator();
		serviceLocator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
		serviceLocator.addService(TransporterFactory.class, FileTransporterFactory.class);
		serviceLocator.addService(TransporterFactory.class, WagonTransporterFactory.class);
		serviceLocator.setServices(WagonProvider.class, new WagonProvider() {
			@Override
			public void release(Wagon wagon) {
				try {
					wagon.disconnect();
				} catch (ConnectionException e) {
					// XXX log exception?
					e.printStackTrace();
				}
			}

			@Override
			public Wagon lookup(String roleHint) throws Exception {
				if ("http".equals(roleHint) || "https".equals(roleHint)) {
					return new HttpWagon();
				}
				throw new UnsupportedOperationException();
			}
		});
		//use wagon instead of HttpTransporterFactory as that fails when we want to deploy artifacts
//		serviceLocator.addService(TransporterFactory.class, HttpTransporterFactory.class);

		serviceLocator.setService(ModelBuilder.class, BugFixModelBuilder.class);
		serviceLocator.setServices(ChecksumPolicyProvider.class, new SupportChecksumPolicyProvider());
		serviceLocator.setServices(LoggerFactory.class);

		serviceLocator.setErrorHandler(new SneakyThrowingErrorHandler());
		return serviceLocator;
	}

	/**
	 * @param config
	 *            May be <code>null</code>. In that case properties related to it are not set.
	 */
	public static DefaultRepositorySystemSession createNewSession(TaskContext taskcontext,
			MavenOperationConfiguration config) {
		DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

		// clear the properties as the system properties shouldn't affect the session.
		//TODO somehow ensure that only the repository session is not modified by system properties
		//the clearing is currenlty removed, as that causes the dependency resolution to fail somewhy.
//		session.setSystemProperties(Collections.emptyMap());
//		session.setConfigProperties(Collections.emptyMap());

		//don't use pom repositories
		session.setIgnoreArtifactDescriptorRepositories(true);
		session.setRepositoryListener(new TaskContextRepositorySessionListener(taskcontext));

		return session;
	}

	public static Exclusion toExclusion(ExclusionOption excloption) {
		String groupid = excloption.getGroupId();
		String artifactid = excloption.getArtifactId();
		String extension = excloption.getExtension();
		String classifier = excloption.getClassifier();
		if (groupid == null) {
			groupid = "*";
		}
		if (artifactid == null) {
			artifactid = "*";
		}
		if (extension == null) {
			extension = "*";
		}
		if (classifier == null) {
			classifier = "*";
		}
		return new Exclusion(groupid, artifactid, classifier, extension);
	}

	public static List<RemoteRepository> createRemoteRepositories(MavenOperationConfiguration config) {
		if (config == null) {
			return Collections.singletonList(getMavenCentralRemoteRepository());
		}
		Set<? extends RepositoryConfiguration> repos = config.getRepositories();
		return createRemoteRepositories(repos);
	}

	public static List<RemoteRepository> createRemoteRepositories(Set<? extends RepositoryConfiguration> repos) {
		if (repos == null) {
			return Collections.singletonList(getMavenCentralRemoteRepository());
		}
		List<RemoteRepository> result = new ArrayList<>();
		for (RepositoryConfiguration repoconfig : repos) {
			RemoteRepository remoterepo = createRemoteRepository(repoconfig);
			result.add(remoterepo);
		}
		return result;
	}

	public static RemoteRepository createRemoteRepository(RepositoryConfiguration repoconfig) {
		//XXX other configurations 
		RemoteRepository.Builder builder = new RemoteRepository.Builder(repoconfig.getId(), repoconfig.getLayout(),
				repoconfig.getUrl());

		builder.setReleasePolicy(toRepositoryPolicy(repoconfig.getReleasePolicy()));
		builder.setSnapshotPolicy(toRepositoryPolicy(repoconfig.getSnapshotPolicy()));

		AuthenticationConfiguration auth = repoconfig.getAuthentication();
		if (auth != null) {
			auth.accept(new AuthenticationConfiguration.Visitor() {
				@Override
				public void visit(AccountAuthenticationConfiguration config) {
					AuthenticationBuilder authbuilder = new AuthenticationBuilder();
					authbuilder.addUsername(config.getUserName());
					authbuilder.addPassword(config.getPassword());
					builder.setAuthentication(authbuilder.build());
				}

				@Override
				public void visit(PrivateKeyAuthenticationConfiguration config) {
					AuthenticationBuilder authbuilder = new AuthenticationBuilder();
					authbuilder.addPrivateKey(config.getKeyLocalPath().toString(), config.getPassPhrase());
					builder.setAuthentication(authbuilder.build());
				}
			});
		}

		RemoteRepository remoterepo = builder.build();
		return remoterepo;
	}

	private static RepositoryPolicy toRepositoryPolicy(RepositoryPolicyConfiguration policyconfig) {
		if (policyconfig == null) {
			return null;
		}
		return new RepositoryPolicy(policyconfig.isEnabled(),
				ObjectUtils.nullDefault(policyconfig.getUpdatePolicy(), DEFAULT_UPDATE_POLICY),
				ObjectUtils.nullDefault(policyconfig.getChecksumPolicy(), DEFAULT_CHECKSUM_POLICY));
	}

	private static final class TaskContextRepositorySessionListener extends AbstractRepositoryListener {
		private final TaskContext taskContext;

		private TaskContextRepositorySessionListener(TaskContext taskcontext) {
			this.taskContext = taskcontext;
		}

		@Override
		public void artifactDownloading(RepositoryEvent event) {
			SakerLog.log().out(taskContext).verbose()
					.println("Downloading artifact: " + event.getArtifact() + " from " + event.getRepository().getId());
		}

		@Override
		public void artifactDownloaded(RepositoryEvent event) {
			Exception exc = event.getException();
			if (exc == null) {
				SakerLog.log().out(taskContext).verbose().println(
						"Downloaded artifact: " + event.getArtifact() + " from " + event.getRepository().getId());
			} else {
				SakerLog.log().out(taskContext).verbose().println("Failed to download artifact: " + event.getArtifact()
						+ " from " + event.getRepository().getId() + " (" + exc + ")");
			}
			File file = event.getFile();
			if (file != null) {
				taskContext.invalidate(LocalFileProvider.getPathKeyStatic(SakerPath.valueOf(file.getAbsolutePath())));
			}
		}

		@Override
		public void artifactInstalled(RepositoryEvent event) {
			File file = event.getFile();
			if (file != null) {
				taskContext.invalidate(LocalFileProvider.getPathKeyStatic(SakerPath.valueOf(file.getAbsolutePath())));
			}
			SakerLog.success().out(taskContext).verbose()
					.println("Installed: " + event.getArtifact() + " to " + event.getRepository().getId());
		}

		@Override
		public void artifactDeploying(RepositoryEvent event) {
			SakerLog.log().out(taskContext).verbose()
					.println("Deploying: " + event.getArtifact() + " to " + event.getRepository().getId());
		}

		@Override
		public void artifactDeployed(RepositoryEvent event) {
			super.artifactDeployed(event);
			Exception exc = event.getException();
			if (exc == null && ObjectUtils.isNullOrEmpty(event.getExceptions())) {
				SakerLog.success().out(taskContext).verbose()
						.println("Deployed: " + event.getArtifact() + " to " + event.getRepository().getId());
			} else {
				SakerLog.error().out(taskContext).verbose().println("Failed to deploy: " + event.getArtifact() + " to "
						+ event.getRepository().getId() + "(" + exc + ")");
			}
		}
	}

	public static ArtifactCoordinates getArtifactCoordinatesFromPom(SakerFile pomfile) throws ModelBuildingException {
		DefaultModelBuildingRequest modelbuildrequest = new DefaultModelBuildingRequest()
				.setModelSource(new ModelSource2() {
					@Override
					public String getLocation() {
						return pomfile.getSakerPath().toString();
					}

					@Override
					public InputStream getInputStream() throws IOException {
						return pomfile.openInputStream();
					}

					@Override
					public ModelSource2 getRelatedSource(String relpath) {
						// not interested
						return null;
					}

					@Override
					public URI getLocationURI() {
						throw new AssertionError("Internal error: ModelSource2.getLocationURI() is unsupported.");
					}
				}).setModelResolver(null);
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
				return new ModelValidator() {
					@Override
					public void validateRawModel(Model model, ModelBuildingRequest arg1, ModelProblemCollector arg2) {
						clearModelForArtifactCoordinateDetermination(model);
					}

					@Override
					public void validateEffectiveModel(Model model, ModelBuildingRequest arg1,
							ModelProblemCollector arg2) {
						clearModelForArtifactCoordinateDetermination(model);
					}
				};
			}
		}.newInstance();

		ModelBuildingResult modelbuildresult = modelbuilder.build(modelbuildrequest);
		Model effectivemodel = modelbuildresult.getEffectiveModel();
		//jar default extension as specified in packaging section of https://maven.apache.org/pom.html
		return new ArtifactCoordinates(effectivemodel.getGroupId(), effectivemodel.getArtifactId(), null,
				ObjectUtils.nullDefault(effectivemodel.getPackaging(), "jar"), effectivemodel.getVersion());
	}

	private static void clearModelForArtifactCoordinateDetermination(Model model) {
		model.setParent(null);

		model.getDependencies().clear();
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

	public static final class SupportChecksumPolicyProvider implements ChecksumPolicyProvider {
		private DefaultChecksumPolicyProvider defaultProvider = new DefaultChecksumPolicyProvider();

		public SupportChecksumPolicyProvider() {
		}

		@Override
		public ChecksumPolicy newChecksumPolicy(RepositorySystemSession session, RemoteRepository repository,
				TransferResource resource, String policy) {
			if (RepositoryPolicy.CHECKSUM_POLICY_IGNORE.equals(policy)) {
				return null;
			}
			if (RepositoryPolicy.CHECKSUM_POLICY_FAIL.equals(policy)) {
				return new FailChecksumPolicy(resource);
			}
			return new WarnChecksumPolicy(resource);
		}

		@Override
		public String getEffectiveChecksumPolicy(RepositorySystemSession session, String policy1, String policy2) {
			return defaultProvider.getEffectiveChecksumPolicy(session, policy1, policy2);
		}

		private abstract class AbstractChecksumPolicy implements ChecksumPolicy {
			protected AbstractChecksumPolicy() {
			}

			@Override
			public boolean onChecksumMatch(String algorithm, int kind) {
				return true;
			}

			@Override
			public void onChecksumMismatch(String algorithm, int kind, ChecksumFailureException exception)
					throws ChecksumFailureException {
				if ((kind & KIND_UNOFFICIAL) == 0) {
					throw exception;
				}
			}

			@Override
			public abstract void onChecksumError(String algorithm, int kind, ChecksumFailureException exception)
					throws ChecksumFailureException;

			@Override
			public void onTransferRetry() {
			}
		}

		private final class FailChecksumPolicy extends AbstractChecksumPolicy {
			protected final TransferResource resource;

			public FailChecksumPolicy(TransferResource resource) {
				this.resource = resource;
			}

			@Override
			public boolean onTransferChecksumFailure(ChecksumFailureException error) {
				return false;
			}

			@Override
			public void onChecksumError(String algorithm, int kind, ChecksumFailureException exception)
					throws ChecksumFailureException {
				throw exception;
			}

			@Override
			public void onNoMoreChecksums() throws ChecksumFailureException {
				throw new ChecksumFailureException("Checksum validation failed, no checksums available for "
						+ resource.getRepositoryUrl() + resource.getResourceName());
			}
		}

		private final class WarnChecksumPolicy extends AbstractChecksumPolicy {
			protected final TransferResource resource;

			public WarnChecksumPolicy(TransferResource resource) {
				this.resource = resource;
			}

			@Override
			public boolean onTransferChecksumFailure(ChecksumFailureException exception) {
				SakerLog.warning().println("Could not validate integrity of download from "
						+ resource.getRepositoryUrl() + resource.getResourceName());
				return true;
			}

			@Override
			public void onChecksumError(String algorithm, int kind, ChecksumFailureException exception)
					throws ChecksumFailureException {
				SakerLog.warning().println("Could not validate " + algorithm + " checksum for "
						+ resource.getRepositoryUrl() + resource.getResourceName() + " (" + exception + ")");
			}

			@Override
			public void onNoMoreChecksums() throws ChecksumFailureException {
				SakerLog.warning().println("Checksum validation failed, no checksums available for "
						+ resource.getRepositoryUrl() + resource.getResourceName());
			}

		}
	}

	public static String getExtensionForPackaging(String packaging) {
		//based on https://maven.apache.org/ref/3.8.6/maven-core/artifact-handlers.html
		//      and org.apache.maven.repository.internal.MavenRepositorySystemUtils.newSession()
		switch (packaging) {
			case "ejb":
			case "maven-plugin":
			case "ejb-client":
			case "java-source":
			case "javadoc":

			case "bundle": // packaging for OSGi bundles? (with maven-bundle-plugin) (like javax.websocket:javax.websocket-api)
			case "kjar": // kjar packaging type, same jar extension (https://developers.redhat.com/blog/2018/03/14/what-is-a-kjar)
				return "jar";
			default:
				return packaging;
		}
	}

	public static String getArtifactTrueExtensionForDependency(
			ModelPackagingCollectorArtifactDescriptorReaderDelegate packagingcollector, Artifact artifact) {
		String extension = artifact.getExtension();
		if ("jar".equals(extension)) {
//				In cases when the dependency type is not specified, Maven assumes jar as the default (per spec)
//				Like:
//				
//			    <dependency>
//			      <groupId>androidx.core</groupId>
//			      <artifactId>core-ktx</artifactId>
//			      <version>1.3.2</version>
//			      <scope>runtime</scope>
//			    </dependency>
//
//				This implicit jar dependency type is passed through the resolved artifact as well, 
//				  so we can check that here in the resolved artifact extension.
//				The above artifact has aar packaging. Therefore, if we use jar extension, it won't be found down the line.
//				Due to this, we fix the extension here.
//				This is more so the fault of the library authors that don't declare <type>aar</type> in their pom, 
//				  but we fix this here. 

			String packaging = packagingcollector.getPackaging(artifact);
			if (!ObjectUtils.isNullOrEmpty(packaging) && !packaging.equals(extension)) {
				//the packaging is specified in the model of the depenency, and it is not jar
				//override the extension with the known extension of the packaging
				extension = getExtensionForPackaging(packaging);
			}
		}
		return extension;
	}

}
