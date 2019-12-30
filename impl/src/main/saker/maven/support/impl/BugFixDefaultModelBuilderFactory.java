package saker.maven.support.impl;

import saker.maven.support.thirdparty.org.apache.maven.model.Model;
import saker.maven.support.thirdparty.org.apache.maven.model.building.DefaultModelBuilderFactory;
import saker.maven.support.thirdparty.org.apache.maven.model.building.ModelBuildingRequest;
import saker.maven.support.thirdparty.org.apache.maven.model.building.ModelProblemCollector;
import saker.maven.support.thirdparty.org.apache.maven.model.plugin.ReportingConverter;
import saker.maven.support.thirdparty.org.apache.maven.model.superpom.SuperPomProvider;

public class BugFixDefaultModelBuilderFactory extends DefaultModelBuilderFactory {
	private static final class EmptySuperPomProvider implements SuperPomProvider {
		private Model superPom = new Model();
		{
			superPom.setModelVersion("4.0.0");
		}

		@Override
		public Model getSuperModel(String version) {
			return superPom;
		}
	}

	private static final class NoOpReportingConverter implements ReportingConverter {
		@Override
		public void convertReporting(Model model, ModelBuildingRequest request, ModelProblemCollector problems) {
			//no-op
		}
	}

	@Override
	protected ReportingConverter newReportingConverter() {
		return new NoOpReportingConverter();
	}

	@Override
	protected SuperPomProvider newSuperPomProvider() {
		return new EmptySuperPomProvider();
	}
}