package saker.maven.support.main.auth;

import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.maven.support.api.MavenOperationConfiguration;
import saker.maven.support.api.MavenOperationConfiguration.AccountAuthenticationConfiguration;
import saker.nest.utils.FrontendTaskFactory;

//TODO doc
public class AccountAuthenticationTaskFactory
		extends FrontendTaskFactory<MavenOperationConfiguration.AccountAuthenticationConfiguration> {
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
				//allow empty username and password
				if (userNameOption == null) {
					taskcontext.abortExecution(new IllegalArgumentException("Null username specified."));
					return null;
				}
				if (passwordOption == null) {
					taskcontext.abortExecution(new IllegalArgumentException("Null password specified."));
					return null;
				}
				AccountAuthenticationConfiguration result = new AccountAuthenticationConfiguration(userNameOption,
						passwordOption);
				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
				return result;
			}
		};
	}

}
