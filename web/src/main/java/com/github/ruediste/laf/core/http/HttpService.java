package com.github.ruediste.laf.core.http;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class HttpService {
	@Inject
	CoreRequestInfo coreRequestInfo;

	public String url(String path) {
		String prefix = coreRequestInfo.getServletRequest().getContextPath();
		prefix += coreRequestInfo.getServletRequest().getServletPath();
		return coreRequestInfo.getServletResponse().encodeURL(prefix + path);
	}

	public String redirectUrl(String path) {
		String prefix = coreRequestInfo.getServletRequest().getContextPath();
		prefix += coreRequestInfo.getServletRequest().getServletPath();
		return coreRequestInfo.getServletResponse().encodeRedirectURL(
				prefix + path);
	}

}
