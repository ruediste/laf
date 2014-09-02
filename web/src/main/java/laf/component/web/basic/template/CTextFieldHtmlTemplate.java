package laf.component.web.basic.template;

import java.io.IOException;

import javax.inject.Inject;

import laf.component.core.basic.CTextField;
import laf.component.web.*;

import org.rendersnake.HtmlAttributesFactory;
import org.rendersnake.HtmlCanvas;

public class CTextFieldHtmlTemplate extends CWTemplateBase<CTextField> {
	@Inject
	CWRenderUtil util;

	@Override
	public void render(CTextField component, HtmlCanvas html)
			throws IOException {
		html.input(HtmlAttributesFactory.type("text")
				.value(component.getValue()).name(util.getKey("value")));
	}

	@Override
	public void applyValues(CTextField component, ApplyValuesUtil util) {
		component.setValue(util.getValue("value"));
	}
}
