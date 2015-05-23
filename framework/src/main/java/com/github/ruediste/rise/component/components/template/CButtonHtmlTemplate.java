package com.github.ruediste.rise.component.components.template;

import static org.rendersnake.HtmlAttributesFactory.class_;

import java.io.IOException;

import javax.inject.Inject;

import org.rendersnake.HtmlCanvas;

import com.github.ruediste.rise.component.ComponentUtil;
import com.github.ruediste.rise.component.components.CButton;
import com.github.ruediste.rise.core.web.CoreAssetBundle;

public class CButtonHtmlTemplate extends ComponentTemplateBase<CButton> {
	@Inject
	ComponentUtil util;

	@Override
	public void doRender(CButton component, HtmlCanvas html) throws IOException {
		html.button(class_(
				util.combineCssClasses("rise_button", component.class_())).data(
				CoreAssetBundle.componentAttributeNr,
				String.valueOf(util.getComponentNr(component))));
		super.doRender(component, html);
		html._button();
	}

	@Override
	public void raiseEvents(CButton component) {
		if (util.isParameterDefined(component, "clicked")
				&& component.getHandler() != null) {
			component.getHandler().run();
		}
	}
}