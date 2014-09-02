package laf.mvc.web;

import laf.core.http.request.HttpRequest;
import laf.mvc.core.ActionPath;

public interface HttpRequestMapper {

	ActionPath<String> parse(HttpRequest request);

	HttpRequest generate(ActionPath<String> path);

}
