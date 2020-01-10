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
package saker.maven.support.main.auth;

import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.maven.support.api.MavenOperationConfiguration;
import saker.maven.support.api.MavenOperationConfiguration.AuthenticationConfiguration;
import saker.maven.support.api.MavenOperationConfiguration.PrivateKeyAuthenticationConfiguration;
import saker.maven.support.main.TaskDocs.DocPrivateKeyAuthenticationConfiguration;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.utils.FrontendTaskFactory;

@NestTaskInformation(returnType = @NestTypeUsage(DocPrivateKeyAuthenticationConfiguration.class))
@NestInformation("Creates a configuration that uses a private key and an associated "
		+ "pass phrase to be authenticate with remote Maven repositories.\n"
		+ "The configuration contains the local file system path to the private key and the pass phrase that can "
		+ "be used with the specified key store.\n"
		+ "The task semantically corresponds to the <privateKey> and <passphrase> pairs in the settings.xml for Maven.")
@NestParameterInformation(value = "KeyLocalPath",
		aliases = { "PrivateKeyLocalPath" },
		required = true,
		type = @NestTypeUsage(SakerPath.class),
		info = @NestInformation("The local file system path to the private key store."))
@NestParameterInformation(value = "Passphrase",
		aliases = { "Password" },
		required = true,
		type = @NestTypeUsage(String.class),
		info = @NestInformation("The passphrase to use for unlocking the keystore."))
public class PrivateKeyAuthenticationTaskFactory
		extends FrontendTaskFactory<MavenOperationConfiguration.AuthenticationConfiguration> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "saker.maven.auth.privatekey";

	@Override
	public ParameterizableTask<? extends AuthenticationConfiguration> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<AuthenticationConfiguration>() {
			//XXX we should accept LocalFileLocation as well
			@SakerInput(value = { "KeyLocalPath", "PrivateKeyLocalPath" }, required = true)
			public SakerPath privateKeyLocalPathOption;
			@SakerInput(value = { "Password", "Passphrase" }, required = true)
			public String passwordOption;

			@Override
			public AuthenticationConfiguration run(TaskContext taskcontext) throws Exception {
				if (privateKeyLocalPathOption == null) {
					taskcontext.abortExecution(new IllegalArgumentException("Null private key local path."));
					return null;
				}
				if (!privateKeyLocalPathOption.isAbsolute()) {
					taskcontext.abortExecution(new IllegalArgumentException(
							"The private key local path must be absolute. (" + privateKeyLocalPathOption + ")"));
					return null;
				}
				//check if valid
				try {
					LocalFileProvider.toRealPath(privateKeyLocalPathOption);
				} catch (Exception e) {
					taskcontext.abortExecution(new IllegalArgumentException(
							"The specified private key local path is not valid for the local file system.", e));
					return null;
				}
				AuthenticationConfiguration result = new PrivateKeyAuthenticationConfiguration(
						privateKeyLocalPathOption, passwordOption);
				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
				return result;
			}
		};
	}

}
