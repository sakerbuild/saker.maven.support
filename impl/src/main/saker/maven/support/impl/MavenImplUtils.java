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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.runtime.execution.SakerLog;
import saker.build.task.TaskContext;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.util.property.SystemPropertyEnvironmentProperty;
import saker.maven.support.api.MavenOperationConfiguration;
import saker.maven.support.api.MavenOperationConfiguration.AccountAuthenticationConfiguration;
import saker.maven.support.api.MavenOperationConfiguration.AuthenticationConfiguration;
import saker.maven.support.api.MavenOperationConfiguration.PrivateKeyAuthenticationConfiguration;
import saker.maven.support.api.MavenOperationConfiguration.RepositoryConfiguration;
import saker.maven.support.api.MavenOperationConfiguration.RepositoryPolicyConfiguration;
import saker.maven.support.impl.dependency.option.ExclusionOption;
import saker.maven.support.thirdparty.org.apache.maven.model.building.ModelBuilder;
import saker.maven.support.thirdparty.org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import saker.maven.support.thirdparty.org.eclipse.aether.AbstractRepositoryListener;
import saker.maven.support.thirdparty.org.eclipse.aether.DefaultRepositorySystemSession;
import saker.maven.support.thirdparty.org.eclipse.aether.RepositoryEvent;
import saker.maven.support.thirdparty.org.eclipse.aether.RepositorySystemSession;
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
import saker.maven.support.thirdparty.org.eclipse.aether.transfer.ChecksumFailureException;
import saker.maven.support.thirdparty.org.eclipse.aether.transfer.TransferResource;
import saker.maven.support.thirdparty.org.eclipse.aether.transport.file.FileTransporterFactory;
import saker.maven.support.thirdparty.org.eclipse.aether.transport.http.HttpTransporterFactory;
import saker.maven.support.thirdparty.org.eclipse.aether.util.repository.AuthenticationBuilder;

public class MavenImplUtils {
	public static final String DEFAULT_CHECKSUM_POLICY = RepositoryPolicy.CHECKSUM_POLICY_WARN;
	public static final String DEFAULT_UPDATE_POLICY = RepositoryPolicy.UPDATE_POLICY_DAILY;
	/**
	 * The URL to the central Maven repository.
	 * <p>
	 * Specified in: <br>
	 * https://maven.apache.org/guides/mini/guide-mirror-settings.html <br>
	 * https://maven.apache.org/ref/3.0.4/maven-model-builder/super-pom.html
	 */
	public static final String MAVEN_CENTRAL_REPOSITORY_URL = "https://repo.maven.apache.org/maven2/";

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
		return new RemoteRepository.Builder("central", "default", MAVEN_CENTRAL_REPOSITORY_URL)
				.setSnapshotPolicy(new RepositoryPolicy(false, null, null)).build();
	}

	public static SakerPath getRepositoryBaseDirectoryDefaulted(TaskContext taskcontext,
			MavenOperationConfiguration config) {
		SakerPath result = config.getLocalRepositoryPath();
		if (result != null) {
			return result;
		}
		return getDefaultMavenLocalRepositoryLocation(taskcontext);
	}

	public static SakerPath getDefaultMavenLocalRepositoryLocation(TaskContext taskcontext) {
		String userhome = taskcontext.getTaskUtilities()
				.getReportEnvironmentDependency(new SystemPropertyEnvironmentProperty("user.home"));
		if (ObjectUtils.isNullOrEmpty(userhome)) {
			throw new IllegalArgumentException(
					"Failed to determine default Maven local repository location. \"user.home\" system property not found.");
		}
		//see also: https://maven.apache.org/settings.html that declares "The default value is ${user.home}/.m2/repository."
		return SakerPath.valueOf(userhome + "/.m2/repository");
	}

	public static DefaultServiceLocator getDefaultServiceLocator() {
		DefaultServiceLocator serviceLocator = MavenRepositorySystemUtils.newServiceLocator();
		serviceLocator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
		serviceLocator.addService(TransporterFactory.class, FileTransporterFactory.class);
		serviceLocator.addService(TransporterFactory.class, HttpTransporterFactory.class);
		serviceLocator.setService(ModelBuilder.class, BugFixModelBuilder.class);
		serviceLocator.setServices(ChecksumPolicyProvider.class, new SupportChecksumPolicyProvider());

		serviceLocator.setErrorHandler(new SneakyThrowingErrorHandler());
		return serviceLocator;
	}

	public static DefaultRepositorySystemSession createNewSession(TaskContext taskcontext,
			MavenOperationConfiguration config) {
		DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

		//clear the properties as the system properties shouldn't affect the session.
		session.setSystemProperties(Collections.emptyMap());
		session.setConfigProperties(Collections.emptyMap());

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
		if (repos == null) {
			return Collections.singletonList(getMavenCentralRemoteRepository());
		}
		List<RemoteRepository> result = new ArrayList<>();
		for (RepositoryConfiguration repoconfig : repos) {
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

			result.add(builder.build());
		}
		return result;
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
		private final TaskContext taskcontext;

		private TaskContextRepositorySessionListener(TaskContext taskcontext) {
			this.taskcontext = taskcontext;
		}

		@Override
		public void artifactDownloading(RepositoryEvent event) {
			SakerLog.log().out(taskcontext).verbose()
					.println("Downloading artifact: " + event.getArtifact() + " from " + event.getRepository().getId());
		}

		@Override
		public void artifactDownloaded(RepositoryEvent event) {
			Exception exc = event.getException();
			if (exc == null) {
				SakerLog.log().out(taskcontext).verbose().println(
						"Downloaded artifact: " + event.getArtifact() + " from " + event.getRepository().getId());
			} else {
				SakerLog.log().out(taskcontext).verbose().println("Failed to download artifact: " + event.getArtifact()
						+ " from " + event.getRepository().getId() + " (" + exc + ")");
			}
			File file = event.getFile();
			if (file != null) {
				taskcontext.invalidate(LocalFileProvider.getPathKeyStatic(SakerPath.valueOf(file.getAbsolutePath())));
			}
		}
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
}
