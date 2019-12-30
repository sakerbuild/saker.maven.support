package saker.maven.support.api.dependency;

/**
 * Interface for limiting the scope of a dependency resolution putput.
 * <p>
 * Callers can use the {@link #get(String)} method to get the resolution output that contains only the specified scopes.
 * <p>
 * Clients shouldn't implement this interface.
 */
public interface MavenDependencyResolutionScopesOutput {
	/**
	 * Gets the dependency resolution output for the specified scopes.
	 * <p>
	 * The argument can be either the following:
	 * <ul>
	 * <li><code>"Compilation"</code>: for the scopes <code>compile</code> and <code>provided</code>.</li>
	 * <li><code>"Execution"</code>: for the scopes <code>compile</code> and <code>runtime</code>.</li>
	 * <li><code>"TestCompilation"</code>: for the scopes <code>test</code>, <code>compile</code> and
	 * <code>provided</code>.</li>
	 * <li><code>"TestExecution"</code>: for the scopes <code>test</code>,<code>compile</code> and
	 * <code>runtime</code>.</li>
	 * <li><code>"&lt;scope&gt;"</code>: a single Maven scope.</li>
	 * <li><code>"&lt;scope&gt;[|&lt;scope&gt;]*"</code>: multiple Maven scopes.</li>
	 * </ul>
	 * The returned resolution output object will only contain dependencies that have any of the specified scopes.
	 * 
	 * @param scope
	 *            The scope to filter for. If <code>null</code>, the result contains no artifacts.
	 * @return The filtered resolution output.
	 */
	public MavenDependencyResolutionTaskOutput get(String scope);
}
