package laf.component.defaultConfiguration;

import java.util.ArrayDeque;
import java.util.ArrayList;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import laf.component.basic.html.CButtonHtmlTemplate;
import laf.component.basic.html.CPageHtmlTemplate;
import laf.component.basic.html.CRenderHtmlTemplate;
import laf.component.basic.html.CTextFieldHtmlTemplate;
import laf.component.html.HtmlPageResultRenderer;
import laf.component.html.template.HtmlTemplate;
import laf.component.html.template.HtmlTemplateFactories;
import laf.component.html.template.HtmlTemplateFactory;
import laf.component.html.template.HtmlTemplateFactoryImpl;
import laf.configuration.ConfigurationDefiner;
import laf.configuration.ExtendConfiguration;
import laf.http.requestProcessing.defaultProcessor.ResultRenderers;

public class DefaultComponentConfiguration implements ConfigurationDefiner {

	@Inject
	Instance<Object> instance;

	@ExtendConfiguration
	public void produce(ResultRenderers renderers) {
		renderers.get()
				.add(instance.select(HtmlPageResultRenderer.class).get());
	}

	@SafeVarargs
	final private <T> Iterable<T> getInstances(Class<? extends T>... classes) {
		ArrayList<T> result = new ArrayList<>();
		for (Class<? extends T> c : classes) {
			result.add(instance.select(c).get());
		}
		return result;
	}

	public void produce(HtmlTemplateFactories factories) {
		ArrayDeque<HtmlTemplateFactory> value = new ArrayDeque<>();
		HtmlTemplateFactoryImpl factory = instance.select(
				HtmlTemplateFactoryImpl.class).get();
		factory.setTemplates(this.<HtmlTemplate<?>> getInstances(
				CButtonHtmlTemplate.class, CPageHtmlTemplate.class,
				CRenderHtmlTemplate.class, CTextFieldHtmlTemplate.class));
		value.add(factory);
		factories.set(value);
	}
}