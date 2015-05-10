package com.github.ruediste.rise.component.web.components.template;

import static org.rendersnake.HtmlAttributesFactory.class_;

import java.io.IOException;

import javax.inject.Inject;

import org.rendersnake.HtmlCanvas;

import com.github.ruediste.rise.component.ComponentUtil;
import com.github.ruediste.rise.component.web.components.CReload;
import com.github.ruediste.rise.core.web.CoreAssetBundle;

public class CReloadHtmlTemplate extends CWTemplateBase<CReload> {
	@Inject
	ComponentUtil util;

	@Override
	public void render(CReload component, HtmlCanvas html) throws IOException {
		html.form(class_("rise_reload").data(
				CoreAssetBundle.componentAttributeNr,
				String.valueOf(util.getComponentNr(component))).data(
				"lwf-reload-count", String.valueOf(component.getReloadCount())));
		super.render(component, html);
		html._form();
	}

	@Override
	public void applyValues(CReload component) {
		component.setReloadCount(component.getReloadCount() + 1);
	}
}