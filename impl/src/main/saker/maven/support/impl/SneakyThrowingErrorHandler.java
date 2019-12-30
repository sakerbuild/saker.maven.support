package saker.maven.support.impl;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.maven.support.thirdparty.org.eclipse.aether.impl.DefaultServiceLocator.ErrorHandler;

public final class SneakyThrowingErrorHandler extends ErrorHandler {
	@Override
	public void serviceCreationFailed(Class<?> type, Class<?> impl, Throwable exception) {
		throw ObjectUtils.sneakyThrow(exception);
	}
}