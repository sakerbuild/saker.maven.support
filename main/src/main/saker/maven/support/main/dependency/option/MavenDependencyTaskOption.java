package saker.maven.support.main.dependency.option;

import java.util.Collection;

import saker.maven.support.main.TaskDocs.DocDependencyScope;
import saker.maven.support.main.dependency.ResolveMavenDependencyTaskFactory;
import saker.nest.scriptinfo.reflection.annot.NestFieldInformation;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;

@NestInformation("Specifies a Maven dependency associated with a given artifact.\n"
		+ "The configuration contains the scope, optionality, and transitive exclusions for a dependency.\n"
		+ "Corresponds to the <dependency/> element in a pom.xml.\n"
		+ "The option also accepts a simple scope string as its input.")
@NestTypeInformation(relatedTypes = @NestTypeUsage(DocDependencyScope.class))
@NestFieldInformation(value = "Scope",
		type = @NestTypeUsage(DocDependencyScope.class),
		info = @NestInformation("Specifies the scope of the dependency.\n"
				+ "Corresponds to the <scope/> element in a pom.xml."))
@NestFieldInformation(value = "Optional",
		type = @NestTypeUsage(boolean.class),
		info = @NestInformation("Specifies the optionality of the dependency.\n"
				+ "Corresponds to the <optional/> element in a pom.xml.\n"
				+ "Generally, this field has no use-case when used with the "
				+ ResolveMavenDependencyTaskFactory.TASK_NAME + "() task, as the dependency is not "
				+ "a transitive dependency of another resolution. However, the field is present for completeness."))
@NestFieldInformation(value = "Exclusions",
		type = @NestTypeUsage(value = Collection.class, elementTypes = ExclusionTaskOption.class),
		info = @NestInformation("Specifies the transitive exclusions of the dependency.\n"
				+ "Corresponds to the <exclusions/> element in a pom.xml."))
public interface MavenDependencyTaskOption {
	public String getScope();

	//this actually has no effect, but we leave it here as the maven API provides this, and can be used in the future
	public Boolean getOptional();

	public Collection<ExclusionTaskOption> getExclusions();

	public static MavenDependencyTaskOption valueOf(String scope) {
		return new ScopeMavenDependencyTaskOption(scope);
	}
}
