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
package saker.maven.support.main.configuration.option;

import saker.maven.support.api.MavenOperationConfiguration.AuthenticationConfiguration;
import saker.maven.support.main.TaskDocs.DocAccountAuthenticationConfiguration;
import saker.maven.support.main.TaskDocs.DocPrivateKeyAuthenticationConfiguration;
import saker.maven.support.main.auth.AccountAuthenticationTaskFactory;
import saker.maven.support.main.auth.PrivateKeyAuthenticationTaskFactory;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;

@NestTypeInformation(relatedTypes = { @NestTypeUsage(DocAccountAuthenticationConfiguration.class),
		@NestTypeUsage(DocPrivateKeyAuthenticationConfiguration.class) })
@NestInformation("Option for authenticating at a remote Maven repository.\n" + "The option accepts the output of the "
		+ AccountAuthenticationTaskFactory.TASK_NAME + "() or the " + PrivateKeyAuthenticationTaskFactory.TASK_NAME
		+ "() tasks.")
public abstract class AuthenticationTaskOption {
	public abstract AuthenticationConfiguration create();

	public static AuthenticationTaskOption valueOf(AuthenticationConfiguration config) {
		return new AuthenticationTaskOption() {
			@Override
			public AuthenticationConfiguration create() {
				return config;
			}
		};
	}
}
