package laf.mvc.web.defaultConfiguration;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import laf.base.configuration.ConfigurationDefiner;
import laf.base.configuration.ExtendConfiguration;
import laf.core.classNameMapping.DefaultClassNameMapping;
import laf.core.defaultConfiguration.ArgumentSerializerChainCP;
import laf.core.defaultConfiguration.HttpRequestParserChainCP;
import laf.mvc.*;
import laf.mvc.web.*;

public class MvcWebDefaultConfiguration implements ConfigurationDefiner {

	@Inject
	Instance<Object> instance;

	public void produce(BasePackageCP val) {
		val.set("");
	}

	public void produce(ControllerNameMappingCP val, BasePackageCP basePackage) {
		DefaultClassNameMapping mapping = instance.select(
				DefaultClassNameMapping.class).get();
		mapping.initialize(basePackage.get(), "Controller");
		val.set(mapping);
	}

	public void produce(HttpRequestMapperCP val,
			ControllerNameMappingCP nameMapping) {
		DefaultHttpRequestMapper mapper = instance.select(
				DefaultHttpRequestMapper.class).get();
		mapper.initialize(nameMapping.get());
		val.set(mapper);
	}

	public void produce(PersistenceRequestHandlerCP val) {
		val.set(instance.select(PersistenceRequestHandler.class).get());
	}

	public void produce(ArgumentLoadingRequestHandlerCP val,
			ArgumentSerializerChainCP serializerChain) {
		ArgumentLoadingRequestHandler handler = instance.select(
				ArgumentLoadingRequestHandler.class).get();
		handler.initialize(serializerChain.get());
		val.set(handler);
	}

	public void produce(ControllerInvokerCP val) {
		val.set(instance.select(ControllerInvoker.class).get());
	}

	public void produce(RequestMappingUtilInitializerCP val,
			HttpRequestMapperCP requestMapperCP,
			ArgumentSerializerChainCP serializerChainCP) {
		RequestMappingUtilInitializer result = instance.select(
				RequestMappingUtilInitializer.class).get();

		result.initialize(requestMapperCP.get(), serializerChainCP.get());
		val.set(result);
	}

	public void produce(RenderResultRendererCP val) {
		val.set(instance.select(ResultRenderer.class).get());
	}

	public void produce(RequestHandlerCP val,
			RequestMappingUtilInitializerCP requestMappingUtilInitializerCV,
			RenderResultRendererCP resultRendererCV,
			PersistenceRequestHandlerCP persistenceCV,
			ArgumentLoadingRequestHandlerCP argumentLoaderCV,
			ControllerInvokerCP invokerCV) {
		requestMappingUtilInitializerCV.get().setDelegate(
				resultRendererCV.get());
		resultRendererCV.get().setDelegate(persistenceCV.get());
		persistenceCV.get().setDelegate(argumentLoaderCV.get());
		argumentLoaderCV.get().setDelegate(invokerCV.get());
		val.set(requestMappingUtilInitializerCV.get());
	}

	public void produce(RequestParserCP val, HttpRequestMapperCP mapper,
			RequestHandlerCP handler) {
		MvcWebRequestParser result = instance.select(MvcWebRequestParser.class)
				.get();
		result.initialize(mapper.get(), handler.get());
		val.set(result);
	}

	@ExtendConfiguration
	public void produce(HttpRequestParserChainCP val, RequestParserCP parser) {
		val.get().parsers.addLast(parser.get());
	}
}
