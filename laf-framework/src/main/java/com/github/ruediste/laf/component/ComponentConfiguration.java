package com.github.ruediste.laf.component;

import java.util.LinkedList;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletResponse;

import com.github.ruediste.laf.api.CView;
import com.github.ruediste.laf.core.ChainedRequestHandler;
import com.github.ruediste.laf.core.CoreConfiguration;
import com.github.ruediste.laf.core.RequestMapper;
import com.github.ruediste.laf.core.web.ActionResultRenderer;
import com.github.ruediste.laf.mvc.web.MvcWebRequestInfo;

@Singleton
public class ComponentConfiguration {

	public Supplier<RequestMapper> mapperSupplier;

	private RequestMapper mapper;

	RequestMapper mapper() {
		return mapper;
	}

	private ChainedRequestHandler handler;

	@Inject
	private CoreConfiguration coreConfiguration;

	public void initialize() {
		mapper = mapperSupplier.get();
		mapper.initialize();
		viewFactory = viewFactorySupplier.get();
		coreConfiguration.actionInvocationToPathInfoMappingFunctions
				.add(invocation -> {
					if (IComponentController.class
							.isAssignableFrom(invocation.methodInvocation
									.getInstanceClass()))
						return Optional.of(mapper.generate(invocation));
					else
						return Optional.empty();
				});

		ChainedRequestHandler last = null;
		for (Supplier<ChainedRequestHandler> supplier : handlerSuppliers) {
			ChainedRequestHandler handler = supplier.get();
			if (this.handler == null)
				this.handler = handler;
			if (last != null)
				last.setNext(handler);
			last = handler;
		}
		if (last != null) {
			last.setNext(finalHandlerSupplier.get());
		}
	}

	public final LinkedList<Supplier<ChainedRequestHandler>> handlerSuppliers = new LinkedList<>();

	/**
	 * Supplier for the final request handler. Initialized with
	 * {@link SupplierRefs#controllerInvokerSupplier}
	 */
	public Supplier<Runnable> finalHandlerSupplier;

	public void handleRequest() {
		handler.run();
	}

	public static class SupplierRefs {
		/**
		 * Supplier for the mapper. By default registered as
		 * {@link ComponentConfiguration#mapperSupplier}
		 */
		public Supplier<RequestMapper> mapperSupplier;

		/**
		 * Handler rendering {@link MvcWebRequestInfo#getActionResult()} to the
		 * {@link HttpServletResponse}
		 */
		public Supplier<ChainedRequestHandler> actionResultRendererSupplier;

		/**
		 * Handler creating the page, including the controller and the view
		 */
		public Supplier<ChainedRequestHandler> pageCreationHandler;

		public Supplier<ChainedRequestHandler> initialPagePersistenceHandler;

		public Supplier<ChainedRequestHandler> viewRenderer;

		/**
		 * Instantiates the controller and invokes the action method. By default
		 * registered as the {@link ComponentConfiguration#finalHandlerSupplier}
		 */
		public Supplier<Runnable> controllerInvokerSupplier;

	}

	public final SupplierRefs supplierRefs = new SupplierRefs();

	@PostConstruct
	public void postConstruct(
			Provider<ComponentRequestMapperImpl> mapper,
			Provider<ComponentControllerInvoker> invoker,
			Provider<ActionResultRenderer> actionResultRenderer,
			Provider<PageCreationHandler> pageCreationHandler,
			Provider<ComponentViewRepository> componentViewRepository,
			Provider<InitialPagePersistenceHandler> initialPagePersistenceHandler,
			Provider<ViewRenderer> viewRenderer) {
		supplierRefs.mapperSupplier = mapper::get;
		this.mapperSupplier = supplierRefs.mapperSupplier;

		supplierRefs.actionResultRendererSupplier = actionResultRenderer::get;
		handlerSuppliers.add(supplierRefs.actionResultRendererSupplier);

		supplierRefs.pageCreationHandler = pageCreationHandler::get;
		handlerSuppliers.add(supplierRefs.pageCreationHandler);

		supplierRefs.initialPagePersistenceHandler = initialPagePersistenceHandler::get;
		handlerSuppliers.add(supplierRefs.initialPagePersistenceHandler);

		supplierRefs.viewRenderer = viewRenderer::get;
		handlerSuppliers.add(supplierRefs.viewRenderer);

		supplierRefs.controllerInvokerSupplier = invoker::get;
		finalHandlerSupplier = supplierRefs.controllerInvokerSupplier;

		viewFactorySupplier = () -> componentViewRepository.get()::createView;
	}

	public Supplier<BiFunction<IComponentController, Class<? extends IViewQualifier>, CView<?>>> viewFactorySupplier;
	public BiFunction<IComponentController, Class<? extends IViewQualifier>, CView<?>> viewFactory;

	public CView<?> createView(IComponentController controller) {
		return createView(controller, null);
	}

	public CView<?> createView(IComponentController controller,
			Class<? extends IViewQualifier> qualifier) {
		return viewFactory.apply(controller, qualifier);
	}

	private String reloadPath = "/~component/reload";

	public String getReloadPath() {
		return reloadPath;
	}
}
