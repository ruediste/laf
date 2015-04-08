package com.github.ruediste.laf.core.entry;

import javax.inject.Named;

import com.github.ruediste.laf.core.classReload.*;
import com.github.ruediste.laf.mvc.web.MvcWebApplicationModule;
import com.github.ruediste.salta.jsr330.AbstractModule;
import com.github.ruediste.salta.jsr330.Provides;

/**
 * Module configuring the permanent injector for LAF
 */
public class ApplicationModule extends AbstractModule {

	@Override
	protected void configure() {
		install(new MvcWebApplicationModule());
	}

	@Named("dynamic")
	@Provides
	SpaceAwareClassLoader spaceAwareClassLoaderDynamic(ClassSpaceCache cache) {
		return new SpaceAwareClassLoader(Thread.currentThread()
				.getContextClassLoader(), DynamicSpace.class, cache,
				PermanentSpace.class);
	}
}