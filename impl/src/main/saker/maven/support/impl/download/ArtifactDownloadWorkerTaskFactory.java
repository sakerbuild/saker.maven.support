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
package saker.maven.support.impl.download;

import java.io.Externalizable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;
import java.util.UUID;

import saker.build.file.SakerDirectory;
import saker.build.file.SakerFile;
import saker.build.file.content.ContentDescriptor;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.file.provider.SakerPathFiles;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.maven.support.api.ArtifactCoordinates;
import saker.maven.support.api.download.ArtifactDownloadWorkerTaskOutput;
import saker.maven.support.main.download.DownloadArtifactsTaskFactory;
import saker.std.api.util.SakerStandardUtils;

public class ArtifactDownloadWorkerTaskFactory implements TaskFactory<ArtifactDownloadWorkerTaskOutput>,
		Task<ArtifactDownloadWorkerTaskOutput>, Externalizable {
	private static final long serialVersionUID = 1L;

	private String repositoryName;
	private SakerPath repositoryBaseDirectory;
	private SakerPath repositoryRelativeArtifactPath;
	private ArtifactCoordinates coordinates;

	/**
	 * For {@link Externalizable}.
	 */
	public ArtifactDownloadWorkerTaskFactory() {
	}

	public ArtifactDownloadWorkerTaskFactory(String workinglocation, SakerPath repositoryBaseDirectory,
			SakerPath repositoryRelativeArtifactPath, ArtifactCoordinates coordinates) {
		SakerPathFiles.requireAbsolutePath(repositoryBaseDirectory);
		SakerPathFiles.requireRelativePath(repositoryRelativeArtifactPath);

		this.repositoryName = ObjectUtils.isNullOrEmpty(workinglocation) ? null : workinglocation;
		this.repositoryBaseDirectory = repositoryBaseDirectory;
		this.repositoryRelativeArtifactPath = repositoryRelativeArtifactPath;
		this.coordinates = coordinates;
	}

	@Override
	public ArtifactDownloadWorkerTaskOutput run(TaskContext taskcontext) throws Exception {
		taskcontext.setStandardOutDisplayIdentifier(DownloadArtifactsTaskFactory.TASK_NAME);

		SakerDirectory builddir = SakerPathFiles.requireBuildDirectory(taskcontext.getExecutionContext());
		SakerDirectory dldir = builddir.getDirectoryCreate(DownloadArtifactsTaskFactory.TASK_NAME);
		if (!ObjectUtils.isNullOrEmpty(repositoryName)) {
			dldir = dldir.getDirectoryCreate(repositoryName);
		}

		LocalFileProvider localfp = LocalFileProvider.getInstance();

		SakerPath artifactpath = repositoryBaseDirectory.resolve(repositoryRelativeArtifactPath);
		ContentDescriptor contentdescriptor = taskcontext.getTaskUtilities().getReportExecutionDependency(
				SakerStandardUtils.createLocalFileContentDescriptorExecutionProperty(artifactpath, UUID.randomUUID()));
		if (contentdescriptor == null) {
			taskcontext.abortExecution(new FileNotFoundException("Artifact not found: " + artifactpath));
			return null;
		}

		String filename = artifactpath.getFileName();
		SakerFile sakerfile = taskcontext.getTaskUtilities().createProviderPathFile(filename,
				localfp.getPathKey(artifactpath), contentdescriptor);

		SakerPath relpath = repositoryRelativeArtifactPath;
		SakerDirectory artifactparentdir = taskcontext.getTaskUtilities().resolveDirectoryAtRelativePathCreate(dldir,
				relpath.getParent());
		SakerFile syncfile;
		while (true) {
			SakerFile prevfile = artifactparentdir.addIfAbsent(sakerfile);
			if (prevfile != null) {
				if (!Objects.equals(contentdescriptor, prevfile.getContentDescriptor())) {
					prevfile.remove();
					continue;
				} else {
					syncfile = prevfile;
				}
			} else {
				syncfile = sakerfile;
			}
			break;
		}
		syncfile.synchronize();
		SakerPath sakerfilepath = artifactparentdir.getSakerPath().resolve(filename);

		taskcontext.reportOutputFileDependency(null, sakerfilepath, contentdescriptor);
		return new ArtifactDownloadWorkerTaskOutputImpl(this.coordinates, sakerfilepath, contentdescriptor);
	}

	@Override
	public Task<? extends ArtifactDownloadWorkerTaskOutput> createTask(ExecutionContext executioncontext) {
		return this;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(repositoryName);
		out.writeObject(repositoryBaseDirectory);
		out.writeObject(repositoryRelativeArtifactPath);
		out.writeObject(coordinates);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		repositoryName = (String) in.readObject();
		repositoryBaseDirectory = (SakerPath) in.readObject();
		repositoryRelativeArtifactPath = (SakerPath) in.readObject();
		coordinates = (ArtifactCoordinates) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((coordinates == null) ? 0 : coordinates.hashCode());
		result = prime * result + ((repositoryBaseDirectory == null) ? 0 : repositoryBaseDirectory.hashCode());
		result = prime * result + ((repositoryName == null) ? 0 : repositoryName.hashCode());
		result = prime * result
				+ ((repositoryRelativeArtifactPath == null) ? 0 : repositoryRelativeArtifactPath.hashCode());
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
		ArtifactDownloadWorkerTaskFactory other = (ArtifactDownloadWorkerTaskFactory) obj;
		if (coordinates == null) {
			if (other.coordinates != null)
				return false;
		} else if (!coordinates.equals(other.coordinates))
			return false;
		if (repositoryBaseDirectory == null) {
			if (other.repositoryBaseDirectory != null)
				return false;
		} else if (!repositoryBaseDirectory.equals(other.repositoryBaseDirectory))
			return false;
		if (repositoryName == null) {
			if (other.repositoryName != null)
				return false;
		} else if (!repositoryName.equals(other.repositoryName))
			return false;
		if (repositoryRelativeArtifactPath == null) {
			if (other.repositoryRelativeArtifactPath != null)
				return false;
		} else if (!repositoryRelativeArtifactPath.equals(other.repositoryRelativeArtifactPath))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "["
				+ (repositoryName != null ? "workingLocation=" + repositoryName + ", " : "")
				+ (repositoryBaseDirectory != null ? "repositoryBaseDirectory=" + repositoryBaseDirectory + ", " : "")
				+ (repositoryRelativeArtifactPath != null
						? "repositoryRelativeArtifactPath=" + repositoryRelativeArtifactPath + ", "
						: "")
				+ (coordinates != null ? "coordinates=" + coordinates : "") + "]";
	}

}
