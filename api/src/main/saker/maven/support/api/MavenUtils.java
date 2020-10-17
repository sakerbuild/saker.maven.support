package saker.maven.support.api;

import java.util.Objects;

import saker.build.exception.MissingConfigurationException;
import saker.build.file.path.SakerPath;
import saker.build.task.TaskContext;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.util.property.SystemPropertyEnvironmentProperty;

/**
 * Utility class related to the saker.maven.support package.
 * 
 * @since saker.maven.support 0.8.4
 */
public class MavenUtils {
	private MavenUtils() {
		throw new UnsupportedOperationException();
	}

	/**
	 * The URL to the central Maven repository.
	 * <p>
	 * Specified in: <br>
	 * https://maven.apache.org/guides/mini/guide-mirror-settings.html <br>
	 * https://maven.apache.org/ref/3.0.4/maven-model-builder/super-pom.html
	 * 
	 * @since saker.maven.support 0.8.7
	 */
	public static final String MAVEN_CENTRAL_REPOSITORY_URL = "https://repo.maven.apache.org/maven2/";

	/**
	 * Gets the default Maven repository location on the local file system.
	 * <p>
	 * It is defined to be: <code>{user.home}/.m2/repository</code>
	 * <p>
	 * If you're calling this method as part of a build task execution, you should use
	 * {@link #getDefaultMavenLocalRepositoryLocation(TaskContext)} as it reports an appropriate dependency as well.
	 * 
	 * @return The default Maven repository local file system path.
	 * @throws MissingConfigurationException
	 *             If the <code>user.home</code> {@linkplain System#getProperty(String) system property} is missing or
	 *             empty.
	 */
	public static SakerPath getDefaultMavenLocalRepositoryLocation() throws MissingConfigurationException {
		String userhome = System.getProperty("user.home");
		return resolveDefaultMavenLocalRepositoryLocation(userhome);
	}

	/**
	 * Gets the default Maven repository location on the local file system and reporty a dependency for it.
	 * <p>
	 * It is defined to be: <code>{user.home}/.m2/repository</code>
	 * <p>
	 * This method is the same as {@link #getDefaultMavenLocalRepositoryLocation()}, but reporty an appropriate
	 * {@linkplain TaskContext#reportEnvironmentDependency(saker.build.runtime.environment.EnvironmentProperty, Object)
	 * environment property dependency} for the build task.
	 * 
	 * @param taskcontext
	 *            The task context of the build task.
	 * @return The default Maven repository local file system path.
	 * @throws NullPointerException
	 *             If the task context is <code>null</code>.
	 * @throws MissingConfigurationException
	 *             If the <code>user.home</code> {@linkplain System#getProperty(String) system property} is missing or
	 *             empty.
	 */
	public static SakerPath getDefaultMavenLocalRepositoryLocation(TaskContext taskcontext)
			throws NullPointerException, MissingConfigurationException {
		Objects.requireNonNull(taskcontext, "task context");
		String userhome = taskcontext.getTaskUtilities()
				.getReportEnvironmentDependency(new SystemPropertyEnvironmentProperty("user.home"));
		return resolveDefaultMavenLocalRepositoryLocation(userhome);
	}

	private static SakerPath resolveDefaultMavenLocalRepositoryLocation(String userhome) {
		if (ObjectUtils.isNullOrEmpty(userhome)) {
			throw new MissingConfigurationException(
					"Failed to determine default Maven local repository location. \"user.home\" system property not found.");
		}
		//see also: https://maven.apache.org/settings.html that declares "The default value is ${user.home}/.m2/repository."
		return SakerPath.valueOf(userhome + "/.m2/repository");
	}
}
