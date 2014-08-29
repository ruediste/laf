package laf.component.web.requestProcessing;

import javax.inject.Inject;

import laf.component.core.RequestHandler;
import laf.component.core.pageScope.PageScopeManager;
import laf.component.core.reqestProcessing.ComponentActionRequest;
import laf.component.web.RequestMappingUtil;
import laf.component.web.RequestMappingUtilInitializer;
import laf.core.http.request.HttpRequest;
import laf.core.requestParserChain.RequestParseResult;
import laf.core.requestParserChain.RequestParser;

public class ComponentWebComponentActionRequestParser implements
		RequestParser<HttpRequest> {

	@Inject
	PageScopeManager pageScopeManager;

	private RequestHandler<ComponentActionRequest> handler;
	private String prefix;

	private RequestMappingUtilInitializer requestMappingUtilInitializer;

	/**
	 * 
	 * @param mapper
	 *            only used to initialize the {@link RequestMappingUtil}
	 */
	public void initialize(String prefix,
			RequestHandler<ComponentActionRequest> handler,
			RequestMappingUtilInitializer requestMappingUtilInitializer) {
		this.prefix = prefix;
		this.handler = handler;
		this.requestMappingUtilInitializer = requestMappingUtilInitializer;

	}

	@Override
	public RequestParseResult<HttpRequest> parse(HttpRequest request) {
		String[] parts = request.getPath().split("/");
		if (!prefix.equals(parts[0])) {
			return null;
		}
		if (parts.length != 3) {
			throw new RuntimeException(
					"expected <reload>/<pageNr>/<componentNr>");
		}

		final ComponentActionRequest actionRequest = new ComponentActionRequest();
		actionRequest.request = request;
		actionRequest.pageNr = Integer.parseInt(parts[1]);
		actionRequest.componentNr = Integer.parseInt(parts[2]);

		return new RequestParseResult<HttpRequest>() {

			@Override
			public void handle(HttpRequest request) {
				requestMappingUtilInitializer.performInitialization();
				pageScopeManager.enter(actionRequest.pageNr);
				try {
					handler.handle(actionRequest);
				} finally {
					pageScopeManager.leave();
				}
			}
		};
	}

}
