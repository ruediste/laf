package com.github.ruediste.rise.api;

import javax.servlet.ServletConfig;

import com.github.ruediste.rise.core.CoreNonRestartableModule;
import com.github.ruediste.rise.nonReloadable.front.LoggerModule;
import com.github.ruediste.salta.jsr330.AbstractModule;

public class PermanentApplicationModule extends AbstractModule {

	protected ServletConfig servletConfig;

	public PermanentApplicationModule(ServletConfig servletConfig) {
		this.servletConfig = servletConfig;
	}

	@Override
	protected void configure() throws Exception {

		installCoreModule();
		installLoggerModule();
	}

	protected void installCoreModule() {
		install(new CoreNonRestartableModule(servletConfig));
	}

	protected void installLoggerModule() {
		install(new LoggerModule());
	}

}
