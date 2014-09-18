package laf.integration;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import laf.component.core.PathActionInvocation;
import laf.component.web.ActionInvocationBuilder;
import laf.component.web.WebRequestInfo;
import laf.component.web.defaultConfiguration.RequestMapperCP;
import laf.core.base.ActionResult;
import laf.core.base.configuration.ConfigurationValue;
import laf.core.defaultConfiguration.ArgumentSerializerChainCP;
import laf.core.http.HttpService;
import laf.mvc.core.PathActionResult;
import laf.mvc.web.ActionPathBuilder;
import laf.mvc.web.RequestMappingUtil;
import laf.mvc.web.defaultConfiguration.HttpRequestMapperCP;

/**
 * Util to be used from other modules to integrate with the MVC Web and the
 * Component Web component
 */
@ApplicationScoped
public class IntegrationUtil {

	@Inject
	HttpService httpService;

	@Inject
	ConfigurationValue<ArgumentSerializerChainCP> serializerChainCV;

	@Inject
	Instance<ActionPathBuilder> mwActionPathBuilderInstance;

	RequestMappingUtil mwMappingUtil;

	@Inject
	ConfigurationValue<HttpRequestMapperCP> mwRequestMapperCV;

	@Inject
	Instance<ActionInvocationBuilder> cwBuilderInstance;

	@Inject
	ConfigurationValue<RequestMapperCP> cwRequestMapperCV;

	WebRequestInfo cwRequestInfo = new WebRequestInfo();

	laf.component.web.RequestMappingUtil cwRequestMappingUtil = new laf.component.web.RequestMappingUtil();

	@PostConstruct
	public void initialize() {
		mwMappingUtil = new RequestMappingUtil();
		mwMappingUtil.initialize(mwRequestMapperCV.value().get(),
				serializerChainCV.value().get());

		cwRequestInfo.setArgumentSerializerChain(serializerChainCV.value()
				.get());
		cwRequestInfo.setRequestMapper(cwRequestMapperCV.value().get());

		cwRequestMappingUtil.webRequestInfo = cwRequestInfo;
	}

	/**
	 * Generate an URL from a servlet path
	 */
	public String url(String servletPath) {
		return httpService.url(servletPath);
	}

	/**
	 * Generate an URL from the result of {@link #mwPath()} or
	 * {@link #mwPath(Class)}. Usage Example
	 *
	 * <pre>
	 * {@code mwUrl(mwPath(<your controller>.class).<action method>())}
	 * </pre>
	 */
	public String mwUrl(ActionResult path) {
		return url(mwServletPath(path));
	}

	public String mwServletPath(ActionResult path) {
		return mwMappingUtil.generate((PathActionResult) path);
	}

	/**
	 * Start generating an an URL targeting a MVC Web controller. Usage Example
	 *
	 * <pre>
	 * {@code mwUrl(mwPath(<your controller>.class).<action method>())}
	 * </pre>
	 */
	public <T> T mwPath(Class<T> controller) {
		return mwPath().controller(controller);
	}

	/**
	 * Start generating an an URL targeting a MVC Web controller. Usage Example
	 *
	 * <pre>
	 * {@code mwUrl(mwPath().controller(<your controller>.class).<action method>())}
	 * </pre>
	 */
	public ActionPathBuilder mwPath() {
		ActionPathBuilder builder = mwActionPathBuilderInstance.get();
		builder.initialize();
		return builder;
	}

	/**
	 * Start generating an an URL targeting a Component Web controller. Usage
	 * Example
	 *
	 * <pre>
	 * {@code cwUrl(cwPath().controller(<your controller>.class).<action method>())}
	 * </pre>
	 */
	public ActionInvocationBuilder cwPath() {
		return cwBuilderInstance.get();
	}

	/**
	 * Start generating an an URL targeting a Component Web controller. Usage
	 * Example
	 *
	 * <pre>
	 * {@code cwUrl(cwPath(<your controller>.class).<action method>())}
	 * </pre>
	 */
	public <T> T cwPath(Class<T> controllerClass) {
		return cwPath().controller(controllerClass);
	}

	/**
	 * Generate an URL from the result of {@link #cwPath()} or
	 * {@link #cwPath(Class)}. Usage Example
	 *
	 * <pre>
	 * {@code cwUrl(cwPath(<your controller>.class).<action method>())}
	 * </pre>
	 */
	public String cwUrl(ActionResult result) {
		return url(cwServletPath(result));
	}

	public String cwServletPath(ActionResult result) {
		return cwRequestMappingUtil.generate((PathActionInvocation) result)
				.getPathWithParameters();
	}
}
