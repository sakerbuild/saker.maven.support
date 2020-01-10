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

import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.maven.support.api.MavenOperationConfiguration;
import saker.maven.support.api.MavenOperationConfiguration.AccountAuthenticationConfiguration;
import saker.maven.support.main.TaskDocs.DocAccountAuthenticationConfiguration;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.utils.FrontendTaskFactory;

@NestTaskInformation(returnType = @NestTypeUsage(DocAccountAuthenticationConfiguration.class))
@NestInformation("Creates an account based authentication configuration to be used with remote Maven repositories.\n"
		+ "The configuration contains an username-password pair that is used to authenticate at a given repository.\n"
		+ "The task semantically corresponds to the <username> and <password> pairs in the settings.xml for Maven.")
@NestParameterInformation(value = "Username",
		aliases = { "User", "UserName" },
		required = true,
		type = @NestTypeUsage(String.class),
		info = @NestInformation("The username to use for authentication."))
@NestParameterInformation(value = "Password",
		required = true,
		type = @NestTypeUsage(String.class),
		info = @NestInformation("The password to use for authentication."))
public class AccountAuthenticationTaskFactory
		extends FrontendTaskFactory<MavenOperationConfiguration.AuthenticationConfiguration> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "saker.maven.auth.account";

	@Override
	public ParameterizableTask<? extends AccountAuthenticationConfiguration> createTask(
			ExecutionContext executioncontext) {
		return new ParameterizableTask<AccountAuthenticationConfiguration>() {
			@SakerInput(value = { "Username", "User", "UserName" }, required = true)
			public String userNameOption;
			@SakerInput(value = { "Password" }, required = true)
			public String passwordOption;

			@Override
			public AccountAuthenticationConfiguration run(TaskContext taskcontext) throws Exception {
				AccountAuthenticationConfiguration result = new AccountAuthenticationConfiguration(userNameOption,
						passwordOption);
				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
				return result;
			}
		};
	}

}
