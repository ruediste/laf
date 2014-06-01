package laf.component.basic.html;

import static org.rendersnake.HtmlAttributesFactory.class_;

import java.io.IOException;

import laf.component.basic.CButton;
import laf.component.html.RenderUtil;
import laf.component.html.template.HtmlTemplateBase;

import org.rendersnake.HtmlCanvas;

public class CButtonHtmlTemplate extends HtmlTemplateBase<CButton> {

	@Override
	public void render(CButton component, HtmlCanvas html, RenderUtil util)
			throws IOException {
		html.button(class_("c_button")).span(class_("_componentId c_hidden"))
		.content(String.valueOf(util.getComponentId()))
		.content(component.getText());
	}

}
