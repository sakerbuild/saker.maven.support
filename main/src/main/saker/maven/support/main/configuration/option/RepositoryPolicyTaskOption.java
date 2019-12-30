package saker.maven.support.main.configuration.option;

import java.util.Objects;

import saker.maven.support.impl.MavenImplUtils;
import saker.maven.support.main.TaskDocs.DocChecksumPolicy;
import saker.maven.support.main.TaskDocs.DocUpdatePolicy;
import saker.maven.support.thirdparty.org.eclipse.aether.repository.RepositoryPolicy;
import saker.nest.scriptinfo.reflection.annot.NestFieldInformation;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;

@NestInformation("Represents an artifact policy configuration for Maven operations.\n"
		+ "The configurations can be defined for release and snapshot artifacts separately in the repository configurations.\n"
		+ "The configuration corresponds to the <releases/> or <snapshots/> elements in a pom.xml.\n"
		+ "The option accepts a simple boolean false as its input to disable the artifact policy.")
@NestFieldInformation(value = "Enabled",
		type = @NestTypeUsage(boolean.class),
		info = @NestInformation("Specifies whether or not the use of the associated artifacts are enabled.\n"
				+ "If not specified, defaults to true."))
@NestFieldInformation(value = "UpdatePolicy",
		type = @NestTypeUsage(DocUpdatePolicy.class),
		info = @NestInformation("Specifies how often the update checks of artifacts should happen from remote repositories.\n"
				+ "May be either one of: " + RepositoryPolicy.UPDATE_POLICY_ALWAYS + ", "
				+ RepositoryPolicy.UPDATE_POLICY_DAILY + ", " + RepositoryPolicy.UPDATE_POLICY_NEVER + " or "
				+ RepositoryPolicy.UPDATE_POLICY_INTERVAL + " in the format of interval:<num-minutes>.\n"
				+ "The default is " + MavenImplUtils.DEFAULT_UPDATE_POLICY + "."))
@NestFieldInformation(value = "ChecksumPolicy",
		type = @NestTypeUsage(DocChecksumPolicy.class),
		info = @NestInformation("Specifies the checksum policy that should be used when incorrect checksums of artifacts are detected.\n"
				+ "May be either one of: " + RepositoryPolicy.CHECKSUM_POLICY_FAIL + ", "
				+ RepositoryPolicy.CHECKSUM_POLICY_WARN + ", " + RepositoryPolicy.CHECKSUM_POLICY_IGNORE + ".\n"
				+ "The default is " + MavenImplUtils.DEFAULT_CHECKSUM_POLICY + "."))
public interface RepositoryPolicyTaskOption {
	public Boolean getEnabled();

	public String getUpdatePolicy();

	public String getChecksumPolicy();

	public static RepositoryPolicyTaskOption valueOf(String str) {
		Objects.requireNonNull(str, "string");
		if (!str.equalsIgnoreCase("false")) {
			throw new IllegalArgumentException("Cannot convert " + str + " to repository policy options.");
		}
		class DisabledRepositoryPolicyOptions implements RepositoryPolicyTaskOption {
			@Override
			public Boolean getEnabled() {
				return false;
			}

			@Override
			public String getUpdatePolicy() {
				return null;
			}

			@Override
			public String getChecksumPolicy() {
				return null;
			}
		}
		return new DisabledRepositoryPolicyOptions();
	}
}