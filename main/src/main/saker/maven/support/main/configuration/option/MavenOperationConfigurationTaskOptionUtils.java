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
package saker.maven.support.main.configuration.option;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import saker.build.file.path.SakerPath;
import saker.build.task.TaskContext;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.maven.support.api.MavenOperationConfiguration;
import saker.maven.support.api.MavenOperationConfiguration.AuthenticationConfiguration;
import saker.maven.support.api.MavenOperationConfiguration.RepositoryConfiguration;
import saker.maven.support.api.MavenOperationConfiguration.RepositoryPolicyConfiguration;
import saker.maven.support.api.MavenUtils;
import saker.maven.support.thirdparty.org.eclipse.aether.repository.RepositoryPolicy;

public class MavenOperationConfigurationTaskOptionUtils {

	//TODO handle argument exceptions in callers
	public static MavenOperationConfiguration createConfiguration(TaskContext taskcontext,
			MavenConfigurationTaskOption options) {
		if (options == null) {
			return MavenOperationConfiguration.defaults(taskcontext);
		}
		return options.createConfiguration(taskcontext);
	}

	static MavenOperationConfiguration createConfigurationImpl(TaskContext taskcontext,
			MavenConfigurationTaskOption options) {
		LocalRepositoryPathTaskOption localrepopathoption = options.getLocalRepositoryPath();
		SakerPath localrepopath = null;
		if (localrepopathoption != null) {
			localrepopath = localrepopathoption.getPath();
		}
		if (localrepopath == null) {
			localrepopath = MavenUtils.getDefaultMavenLocalRepositoryLocation(taskcontext);
		}
		MavenOperationConfiguration.Builder builder = MavenOperationConfiguration.builder(localrepopath);
		Collection<RepositoryTaskOption> repos = options.getRepositories();
		if (repos != null) {
			Set<RepositoryConfiguration> repoconfigs = new LinkedHashSet<>();
			for (RepositoryTaskOption repooptions : repos) {
				RepositoryConfiguration repoconfig = createRepositoryConfiguration(repooptions);
				repoconfigs.add(repoconfig);
			}
			builder.setRepositories(repoconfigs);
		} else {
			builder.setRepositories(Collections.singleton(RepositoryConfiguration.DEFAULT_MAVEN_CENTRAL));
		}
		return builder.build();
	}

	public static RepositoryConfiguration createRepositoryConfiguration(RepositoryTaskOption repooptions) {
		RepositoryPolicyConfiguration snapshotpolicy = createRepositoryPolicyConfig(repooptions.getSnapshots());
		RepositoryPolicyConfiguration releasepolicy = createRepositoryPolicyConfig(repooptions.getReleases());

		AuthenticationTaskOption authtasktoption = repooptions.getAuthentication();
		AuthenticationConfiguration auth;
		if (authtasktoption != null) {
			auth = authtasktoption.create();
		} else {
			auth = null;
		}

		RepositoryConfiguration repoconfig = new RepositoryConfiguration(repooptions.getId(), repooptions.getLayout(),
				repooptions.getUrl(), snapshotpolicy, releasepolicy, auth);
		return repoconfig;
	}

	private static final Set<String> ALLOWED_CHECKSUM_POLICIES = ImmutableUtils
			.makeImmutableNavigableSet(new String[] { RepositoryPolicy.CHECKSUM_POLICY_FAIL,
					RepositoryPolicy.CHECKSUM_POLICY_IGNORE, RepositoryPolicy.CHECKSUM_POLICY_WARN });

	private static final Set<String> ALLOWED_UPDATE_POLICIES = ImmutableUtils
			.makeImmutableNavigableSet(new String[] { RepositoryPolicy.UPDATE_POLICY_ALWAYS,
					RepositoryPolicy.UPDATE_POLICY_DAILY, RepositoryPolicy.UPDATE_POLICY_NEVER });
	private static final Pattern PATTERN_UPDATE_POLICY_PATTERN = Pattern.compile("interval:[0-9]+");

	private static RepositoryPolicyConfiguration createRepositoryPolicyConfig(RepositoryPolicyTaskOption policy) {
		if (policy == null) {
			return null;
		}
		String checksumpolicy = policy.getChecksumPolicy();
		if (checksumpolicy != null) {
			if (!ALLOWED_CHECKSUM_POLICIES.contains(checksumpolicy)) {
				throw new IllegalArgumentException("Invalid checksum policy: " + checksumpolicy + " allowed: "
						+ StringUtils.toStringJoin(", ", ALLOWED_CHECKSUM_POLICIES));
			}
		}
		String updatepolicy = policy.getUpdatePolicy();
		if (updatepolicy != null) {
			if (!ALLOWED_UPDATE_POLICIES.contains(updatepolicy)
					&& !PATTERN_UPDATE_POLICY_PATTERN.matcher(updatepolicy).matches()) {
				throw new IllegalArgumentException("Invalid update policy: " + updatepolicy + " allowed: "
						+ StringUtils.toStringJoin(", ", ALLOWED_UPDATE_POLICIES) + ", interval:<num-minutes>");
			}
		}
		return RepositoryPolicyConfiguration.create(ObjectUtils.defaultize(policy.getEnabled(), true), updatepolicy,
				checksumpolicy);
	}

	private MavenOperationConfigurationTaskOptionUtils() {
		throw new UnsupportedOperationException();
	}
}
