package saker.maven.support.main.dependency.option;

import java.util.Collection;

final class ScopeMavenDependencyTaskOption implements MavenDependencyTaskOption {
	private final String scope;

	ScopeMavenDependencyTaskOption(String scope) {
		this.scope = scope;
	}

	@Override
	public String getScope() {
		return scope;
	}

	@Override
	public Boolean getOptional() {
		return null;
	}

	@Override
	public Collection<ExclusionTaskOption> getExclusions() {
		return null;
	}
}