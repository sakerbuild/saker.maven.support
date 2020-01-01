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
package saker.maven.support.api;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;
import java.util.Set;

import saker.build.file.path.SakerPath;
import saker.build.file.provider.SakerPathFiles;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;

/**
 * Holds the configuration information for operations that work with Maven repositories.
 * <p>
 * The class contains the configuration data that should be used to setup and initialize the associated operation. It
 * determines which repositories and how they should be used.
 * <p>
 * Cliens can use {@link #builder()} to create a new configuration or {@link #defaults()} to get the default
 * configuration.
 */
public final class MavenOperationConfiguration implements Externalizable {
	/**
	 * Artifact policy configuration for Maven operations.
	 * <p>
	 * The class holds information on how the operation should interact with the release or snapshot artifacts from a
	 * repository.
	 * <p>
	 * The class corresponds to the &lt;releases/&gt; or &lt;snapshots/&gt; elements in a pom.xml.
	 */
	public static final class RepositoryPolicyConfiguration implements Externalizable {
		private static final RepositoryPolicyConfiguration DISABLED_INSTANCE = new RepositoryPolicyConfiguration();

		private static final long serialVersionUID = 1L;

		private boolean enabled;
		private String updatePolicy;
		private String checksumPolicy;

		/**
		 * For {@link Externalizable}.
		 * 
		 * @deprecated Use other constructors.
		 */
		@Deprecated
		public RepositoryPolicyConfiguration() {
		}

		/**
		 * Gets a policy configuration that disables the associated artifacts.
		 * 
		 * @return The disabled configuration.
		 */
		public static RepositoryPolicyConfiguration disabled() {
			return DISABLED_INSTANCE;
		}

		/**
		 * Gets a policy configuration that enables the associated artifacts and uses the specified update and checksum
		 * policy.
		 * 
		 * @param updatePolicy
		 *            The update policy.
		 * @param checksumPolicy
		 *            The checksum policy.
		 * @return The policy configuration.
		 */
		public static RepositoryPolicyConfiguration enabled(String updatePolicy, String checksumPolicy) {
			return new RepositoryPolicyConfiguration(updatePolicy, checksumPolicy);
		}

		/**
		 * Creates a new policy configuration with the specified properties.
		 * <p>
		 * The method is a convenience method to create a configuration with a boolean enablement parameter. If enabled,
		 * then it is the same as:
		 * 
		 * <pre>
		 * enabled(updatePolicy, checksumPolicy);
		 * </pre>
		 * 
		 * If not enabled, then:
		 * 
		 * <pre>
		 * disabled();
		 * </pre>
		 *
		 * @param enabled
		 *            Whether or not the associated artifacts are enabled.
		 * @param updatePolicy
		 *            The update policy.
		 * @param checksumPolicy
		 *            The checksum policy.
		 * @return The policy configuration.
		 */
		public static RepositoryPolicyConfiguration create(boolean enabled, String updatePolicy,
				String checksumPolicy) {
			if (!enabled) {
				return disabled();
			}
			return enabled(updatePolicy, checksumPolicy);
		}

		private RepositoryPolicyConfiguration(String updatePolicy, String checksumPolicy) {
			this.enabled = true;
			this.updatePolicy = updatePolicy;
			this.checksumPolicy = checksumPolicy;
		}

		/**
		 * Checks if the associated artifacts are enabled or not.
		 * 
		 * @return <code>true</code> if the artifacts are enabled.
		 */
		public boolean isEnabled() {
			return enabled;
		}

		/**
		 * Gets the update policy.
		 * <p>
		 * The update policy is either of the following:
		 * <ul>
		 * <li><code>never</code>: Never update locally cached data.</li>
		 * <li><code>always</code>: Always update locally cached data.</li>
		 * <li><code>daily</code>: Update locally cached data once a day.</li>
		 * <li><code>interval:X</code>: Update locally cached data every X minutes.</li>
		 * </ul>
		 * <p>
		 * Note that the returned value may not be in any of the above format. It is directly passed to the Maven
		 * backend.
		 * 
		 * @return The update policy or <code>null</code> to use an implementation dependent default.
		 */
		public String getUpdatePolicy() {
			return updatePolicy;
		}

		/**
		 * Gets the checksum policy.
		 * <p>
		 * It is either of the following:
		 * <ul>
		 * <li><code>fail</code>: Verify checksums and fail the resolution if they do not match.</li>
		 * <li><code>warn</code>: Verify checksums and warn if they do not match.</li>
		 * <li><code>ignore</code>: Do not verify checksums.</li>
		 * </ul>
		 * Note that the returned value may not be in any of the above format. It is directly passed to the Maven
		 * backend.
		 * 
		 * @return The checksum policy or <code>null</code> to use an implementation dependent default.
		 */
		public String getChecksumPolicy() {
			return checksumPolicy;
		}

		//for compatibility with MavenConfigurationTaskOption
		/**
		 * @see #isEnabled()
		 */
		public boolean getEnabled() {
			return enabled;
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeBoolean(enabled);
			out.writeObject(updatePolicy);
			out.writeObject(checksumPolicy);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			enabled = in.readBoolean();
			updatePolicy = (String) in.readObject();
			checksumPolicy = (String) in.readObject();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((checksumPolicy == null) ? 0 : checksumPolicy.hashCode());
			result = prime * result + (enabled ? 1231 : 1237);
			result = prime * result + ((updatePolicy == null) ? 0 : updatePolicy.hashCode());
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
			RepositoryPolicyConfiguration other = (RepositoryPolicyConfiguration) obj;
			if (checksumPolicy == null) {
				if (other.checksumPolicy != null)
					return false;
			} else if (!checksumPolicy.equals(other.checksumPolicy))
				return false;
			if (enabled != other.enabled)
				return false;
			if (updatePolicy == null) {
				if (other.updatePolicy != null)
					return false;
			} else if (!updatePolicy.equals(other.updatePolicy))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "[enabled=" + enabled + ", "
					+ (updatePolicy != null ? "updatePolicy=" + updatePolicy + ", " : "")
					+ (checksumPolicy != null ? "checksumPolicy=" + checksumPolicy : "") + "]";
		}

	}

	/**
	 * Contains configurations for a remote Maven repository.
	 * <p>
	 * The class corresponds to the &lt;repository/&gt; element in the pom.xml.
	 */
	public static final class RepositoryConfiguration implements Externalizable {
		private static final long serialVersionUID = 1L;

		private String id;
		private String layout;
		private String url;

		private RepositoryPolicyConfiguration snapshotPolicy;
		private RepositoryPolicyConfiguration releasePolicy;

		/**
		 * For {@link Externalizable}.
		 * 
		 * @deprecated Use other constructors.
		 */
		@Deprecated
		public RepositoryConfiguration() {
		}

		/**
		 * Creates a new configuration with the specified properties and default snapshot and release policies.
		 * <p>
		 * The snapshot and release policies will use the defaults provided by the Maven backed. They are
		 * {@linkplain RepositoryPolicyConfiguration#isEnabled() enabled} with
		 * {@linkplain RepositoryPolicyConfiguration#getUpdatePolicy() daily update policy} and
		 * {@linkplain RepositoryPolicyConfiguration#getChecksumPolicy() warnig checksum policy}.
		 * 
		 * @param id
		 *            The repository ID.
		 * @param layout
		 *            The repository layout type. Either <code>"default"</code> or <code>"legacy"</code>. If
		 *            <code>null</code>, it will be set to <code>"default"</code>.
		 * @param url
		 *            The repository URL.
		 * @throws NullPointerException
		 *             If the URL is <code>null</code>.
		 */
		public RepositoryConfiguration(String id, String layout, String url) throws NullPointerException {
			this(id, layout, url, null, null);
		}

		/**
		 * Creates a new configuration with the specified properties.
		 * 
		 * @param id
		 *            The repository ID.
		 * @param layout
		 *            The repository layout type. Either <code>"default"</code> or <code>"legacy"</code>. If
		 *            <code>null</code>, it will be set to <code>"default"</code>.
		 * @param url
		 *            The repository URL.
		 * @param snapshotPolicy
		 *            The snapshot policy.
		 * @param releasePolicy
		 *            The release policy.
		 * @throws NullPointerException
		 *             If the URL is <code>null</code>.
		 */
		public RepositoryConfiguration(String id, String layout, String url,
				RepositoryPolicyConfiguration snapshotPolicy, RepositoryPolicyConfiguration releasePolicy)
				throws NullPointerException {
			Objects.requireNonNull(url, "Maven repository URL");
			this.id = id;
			this.layout = layout == null ? "default" : layout;
			this.url = url;
			this.snapshotPolicy = snapshotPolicy;
			this.releasePolicy = releasePolicy;
		}

		/**
		 * Gets the repository identifier.
		 * <p>
		 * The identifier should uniquely identify the repository configuration in the associated context.
		 * <p>
		 * Corresponds to the &lt;id/&gt; element in the pom.xml &lt;repository/&gt; configuration.
		 * 
		 * @return The repository identifier.
		 */
		public String getId() {
			return id;
		}

		/**
		 * Gets the repository layout.
		 * <p>
		 * The value <code>"legacy"</code> means layout for repositories used by Maven 1.x. The value
		 * <code>"default"</code> are used by Maven 2 &amp; 3.
		 * <p>
		 * Corresponds to the &lt;layout/&gt; element in the pom.xml &lt;repository/&gt; configuration.
		 * 
		 * @return The layout.
		 */
		public String getLayout() {
			return layout;
		}

		/**
		 * Gets the URL to the remote repository.
		 * <p>
		 * It specifies both the location and the transport protocol used to transfer a built artifact (and POM file,
		 * and checksum data) to the repository.
		 * <p>
		 * Corresponds to the &lt;url/&gt; element in the pom.xml &lt;repository/&gt; configuration.
		 * 
		 * @return The URL.
		 */
		public String getUrl() {
			return url;
		}

		/**
		 * Gets the snapshot policy.
		 * <p>
		 * Specifies how the repository should interact with snapshot artifacts.
		 * <p>
		 * Corresponds to the &lt;snapshots/&gt; element in the pom.xml &lt;repository/&gt; configuration.
		 * 
		 * @return The policy or <code>null</code> if the
		 *             {@linkplain RepositoryConfiguration#RepositoryConfiguration(String, String, String) default} is
		 *             used.
		 */
		public RepositoryPolicyConfiguration getSnapshotPolicy() {
			return snapshotPolicy;
		}

		/**
		 * Gets the release policy.
		 * <p>
		 * Specifies how the repository should interact with release artifacts.
		 * <p>
		 * Corresponds to the &lt;releases/&gt; element in the pom.xml &lt;repository/&gt; configuration.
		 * 
		 * @return The policy or <code>null</code> if the
		 *             {@linkplain RepositoryConfiguration#RepositoryConfiguration(String, String, String) default} is
		 *             used.
		 */
		public RepositoryPolicyConfiguration getReleasePolicy() {
			return releasePolicy;
		}

		//for compatibility with MavenConfigurationTaskOption
		/**
		 * @see #getSnapshotPolicy()
		 */
		public RepositoryPolicyConfiguration getSnapshots() {
			return snapshotPolicy;
		}

		//for compatibility with MavenConfigurationTaskOption
		/**
		 * @see #getReleasePolicy()
		 */
		public RepositoryPolicyConfiguration getReleases() {
			return releasePolicy;
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeObject(id);
			out.writeObject(layout);
			out.writeObject(url);
			out.writeObject(snapshotPolicy);
			out.writeObject(releasePolicy);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			id = (String) in.readObject();
			layout = (String) in.readObject();
			url = (String) in.readObject();
			snapshotPolicy = (RepositoryPolicyConfiguration) in.readObject();
			releasePolicy = (RepositoryPolicyConfiguration) in.readObject();
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(id);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			RepositoryConfiguration other = (RepositoryConfiguration) obj;
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id))
				return false;
			if (releasePolicy == null) {
				if (other.releasePolicy != null)
					return false;
			} else if (!releasePolicy.equals(other.releasePolicy))
				return false;
			if (snapshotPolicy == null) {
				if (other.snapshotPolicy != null)
					return false;
			} else if (!snapshotPolicy.equals(other.snapshotPolicy))
				return false;
			if (layout == null) {
				if (other.layout != null)
					return false;
			} else if (!layout.equals(other.layout))
				return false;
			if (url == null) {
				if (other.url != null)
					return false;
			} else if (!url.equals(other.url))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "[" + (id != null ? "id=" + id + ", " : "")
					+ (layout != null ? "layout=" + layout + ", " : "") + (url != null ? "url=" + url + ", " : "")
					+ (snapshotPolicy != null ? "snapshotPolicy=" + snapshotPolicy + ", " : "")
					+ (releasePolicy != null ? "releasePolicy=" + releasePolicy : "") + "]";
		}

	}

	private static final long serialVersionUID = 1L;

	private static final MavenOperationConfiguration DEFAULTS_INSTANCE = new MavenOperationConfiguration();

	private SakerPath localRepositoryPath;
	private Set<RepositoryConfiguration> repositories;

	/**
	 * For {@link Externalizable}.
	 * 
	 * @deprecated Use {@link #builder()}.
	 */
	@Deprecated
	public MavenOperationConfiguration() {
	}

	/**
	 * Gets the local file system path for the local repository.
	 * <p>
	 * The default is: <code>{user.dir}/.m2/repository</code>
	 * 
	 * @return The local path or <code>null</code> if the default should be used.
	 */
	public SakerPath getLocalRepositoryPath() {
		return localRepositoryPath;
	}

	/**
	 * Gets the remote repositories that should be used for the operation.
	 * <p>
	 * The default is <code>https://repo.maven.apache.org/maven2/</code> with the ID of <code>central</code> and layout
	 * <code>default</code>. The snapshots are disabled.
	 * 
	 * @return The repositories or <code>null</code> if the default should be used.
	 */
	public Set<? extends RepositoryConfiguration> getRepositories() {
		return repositories;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(localRepositoryPath);
		SerialUtils.writeExternalCollection(out, repositories);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		localRepositoryPath = (SakerPath) in.readObject();
		repositories = SerialUtils.readExternalImmutableLinkedHashSet(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((localRepositoryPath == null) ? 0 : localRepositoryPath.hashCode());
		result = prime * result + ((repositories == null) ? 0 : repositories.hashCode());
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
		MavenOperationConfiguration other = (MavenOperationConfiguration) obj;
		if (localRepositoryPath == null) {
			if (other.localRepositoryPath != null)
				return false;
		} else if (!localRepositoryPath.equals(other.localRepositoryPath))
			return false;
		if (repositories == null) {
			if (other.repositories != null)
				return false;
		} else if (!repositories.equals(other.repositories))
			return false;
		return true;
	}

	/**
	 * Creates a new {@link MavenOperationConfiguration} builder.
	 * 
	 * @return The new builder.
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Gets the Maven operation configuration that causes the operation to use the defaults.
	 * <p>
	 * See the default values in the associated getter method.
	 * <p>
	 * The getter methods will all return <code>null</code> to signal that the default should be used.
	 * 
	 * @return The defaults configuration.
	 */
	public static MavenOperationConfiguration defaults() {
		return DEFAULTS_INSTANCE;
	}

	/**
	 * A builder class for creating a {@link MavenOperationConfiguration}.
	 */
	public static final class Builder {
		private SakerPath localRepositoryPath;
		private Set<RepositoryConfiguration> repositories;

		protected Builder() {
		}

		/**
		 * Sets the {@linkplain MavenOperationConfiguration#getLocalRepositoryPath() local repository path}.
		 * 
		 * @param localRepositoryPath
		 *            The local repository path or <code>null</code> to reset to the
		 *            {@linkplain MavenOperationConfiguration#defaults() defaults}.
		 * @return <code>this</code>
		 * @throws IllegalArgumentException
		 *             If the argument is not absolute.
		 */
		public Builder setLocalRepositoryPath(SakerPath localRepositoryPath) throws IllegalArgumentException {
			if (localRepositoryPath != null) {
				SakerPathFiles.requireAbsolutePath(localRepositoryPath);
			}
			this.localRepositoryPath = localRepositoryPath;
			return this;
		}

		/**
		 * Sets the {@linkplain MavenOperationConfiguration#getRepositories() repositories}.
		 * 
		 * @param repositories
		 *            The repositories or <code>null</code> to reset to the
		 *            {@linkplain MavenOperationConfiguration#defaults() defaults}.
		 * @return <code>this</code>
		 */
		public Builder setRepositories(Set<? extends RepositoryConfiguration> repositories) {
			this.repositories = ImmutableUtils.makeImmutableLinkedHashSet(repositories);
			return this;
		}

		/**
		 * Builds the {@link MavenOperationConfiguration}.
		 * <p>
		 * The builder can be reused after this call.
		 * 
		 * @return The created Maven operation configuration.
		 */
		public MavenOperationConfiguration build() {
			MavenOperationConfiguration result = new MavenOperationConfiguration();
			result.localRepositoryPath = this.localRepositoryPath;
			result.repositories = repositories;
			return result;
		}
	}
}
