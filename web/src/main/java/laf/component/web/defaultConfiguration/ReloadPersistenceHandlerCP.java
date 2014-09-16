package laf.component.web.defaultConfiguration;

import laf.component.core.DelegatingRequestHandler;
import laf.component.core.PageReloadRequest;
import laf.core.base.configuration.ConfigurationParameter;

public interface ReloadPersistenceHandlerCP
		extends
		ConfigurationParameter<DelegatingRequestHandler<PageReloadRequest, PageReloadRequest>> {

}