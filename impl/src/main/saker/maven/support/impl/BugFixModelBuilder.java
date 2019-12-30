package saker.maven.support.impl;

import java.io.File;

import saker.maven.support.thirdparty.org.apache.maven.model.Model;
import saker.maven.support.thirdparty.org.apache.maven.model.building.DefaultModelBuilder;
import saker.maven.support.thirdparty.org.apache.maven.model.building.ModelBuilder;
import saker.maven.support.thirdparty.org.apache.maven.model.building.ModelBuildingException;
import saker.maven.support.thirdparty.org.apache.maven.model.building.ModelBuildingRequest;
import saker.maven.support.thirdparty.org.apache.maven.model.building.ModelBuildingResult;
import saker.maven.support.thirdparty.org.apache.maven.model.building.Result;

/**
 * This {@link ModelBuilder} class makes the <code>maven-resolver</code> library not encounter the bugs related to:
 * 
 * <pre>
 * this.getClass().getPackage().getImplementationVersion()
 * </pre>
 * 
 * As the {@link Package Packages} may not be defiend for the classes.
 * <p>
 * Keep the class and constructor <code>public</code> to allow instantiation via reflection.
 * 
 * @see https://issues.apache.org/jira/browse/MRESOLVER-94
 */
public class BugFixModelBuilder implements ModelBuilder {
	private final DefaultModelBuilder builder = new BugFixDefaultModelBuilderFactory().newInstance();

	public BugFixModelBuilder() {
	}

	@Override
	public Result<? extends Model> buildRawModel(File pomFile, int validationLevel, boolean locationTracking) {
		return builder.buildRawModel(pomFile, validationLevel, locationTracking);
	}

	@Override
	public ModelBuildingResult build(ModelBuildingRequest request, ModelBuildingResult result)
			throws ModelBuildingException {
		return builder.build(request, result);
	}

	@Override
	public ModelBuildingResult build(ModelBuildingRequest request) throws ModelBuildingException {
		return builder.build(request);
	}
}