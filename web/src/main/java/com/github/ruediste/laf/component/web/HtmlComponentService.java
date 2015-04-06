package com.github.ruediste.laf.component.web;

import javax.servlet.http.HttpServletResponse;

import com.github.ruediste.laf.component.core.api.CView;
import com.github.ruediste.laf.component.core.tree.Component;

public interface HtmlComponentService {

	public abstract String calculateKey(Component component, String key);

	public abstract void renderPage(CView<?> view,
			Component rootComponent, HttpServletResponse response);

	public abstract long getComponentNr(Component component);

	public abstract Component getComponent(CView<?> view,
			long componentId);

}