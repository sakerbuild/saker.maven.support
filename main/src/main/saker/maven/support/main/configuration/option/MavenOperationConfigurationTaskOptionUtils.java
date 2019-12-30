package saker.maven.support.main.configuration.option;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.maven.support.api.MavenOperationConfiguration;
import saker.maven.support.api.MavenOperationConfiguration.RepositoryConfiguration;
import saker.maven.support.api.MavenOperationConfiguration.RepositoryPolicyConfiguration;
import saker.maven.support.thirdparty.org.eclipse.aether.repository.RepositoryPolicy;

public class MavenOperationConfigurationTaskOptionUtils {

	//TODO handle argument exceptions in callers
	public static MavenOperationConfiguration createConfiguration(MavenConfigurationTaskOption options) {
		if (options == null) {
			return MavenOperationConfiguration.defaults();
		}
		MavenOperationConfiguration.Builder builder = MavenOperationConfiguration.builder();
		builder.setLocalRepositoryPath(options.getLocalRepositoryPath());
		Collection<RepositoryTaskOption> repos = options.getRepositories();
		if (repos != null) {
			Set<RepositoryConfiguration> repoconfigs = new LinkedHashSet<>();
			for (RepositoryTaskOption repooptions : repos) {
				RepositoryPolicyConfiguration snapshotpolicy = createRepositoryPolicyConfig(repooptions.getSnapshots());
				RepositoryPolicyConfiguration releasepolicy = createRepositoryPolicyConfig(repooptions.getReleases());
				RepositoryConfiguration repoconfig = new RepositoryConfiguration(repooptions.getId(),
						repooptions.getLayout(), repooptions.getUrl(), snapshotpolicy, releasepolicy);
				repoconfigs.add(repoconfig);
			}
			builder.setRepositories(repoconfigs);
		}
		return builder.build();
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
