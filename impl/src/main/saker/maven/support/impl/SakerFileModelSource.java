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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Objects;

import saker.build.file.SakerDirectory;
import saker.build.file.SakerFile;
import saker.build.file.path.SakerPath;
import saker.build.task.TaskContext;
import saker.maven.support.thirdparty.org.apache.maven.model.building.ModelSource2;

public class SakerFileModelSource implements ModelSource2 {
	private final TaskContext taskContext;
	private final SakerFile file;

	public SakerFileModelSource(TaskContext taskContext, SakerFile file) {
		this.taskContext = taskContext;
		this.file = file;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return file.openInputStream();
	}

	@Override
	public String getLocation() {
		return Objects.toString(file.getSakerPath());
	}

	@Override
	public ModelSource2 getRelatedSource(String relPath) {
		SakerPath relative = SakerPath.valueOf(relPath);
		SakerFile resolvedfile = taskContext.getTaskUtilities().resolveAtRelativePath(file.getParent(), relative);
		if (resolvedfile == null) {
			return null;
		}
		if (resolvedfile instanceof SakerDirectory) {
			SakerDirectory resolveddir = (SakerDirectory) resolvedfile;
			resolvedfile = resolveddir.get("pom.xml");
			if (resolvedfile instanceof SakerDirectory) {
				return null;
			}
		}
		taskContext.getTaskUtilities().reportInputFileDependency(null, resolvedfile);
		return new SakerFileModelSource(taskContext, resolvedfile);
	}

	@Override
	public URI getLocationURI() {
		throw new AssertionError("Internal error: ModelSource2.getLocationURI() is unsupported.");
	}

}