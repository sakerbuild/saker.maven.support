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