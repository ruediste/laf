package laf.httpRequestMapping.parameterHandler;

import laf.controllerInfo.ParameterInfo;
import laf.httpRequestMapping.HttpRequestMappingRule;
import laf.httpRequestMapping.parameterValueProvider.ParameterValueProvider;

/**
 * Maps parameters to URL strings. Used by {@link HttpRequestMappingRule}s to
 * handle parameters.
 *
 * @see ParameterHandlerModule
 */
public interface ParameterHandler {

	/**
	 * Returns true if this handler can handle the specified parameter.
	 */
	boolean handles(ParameterInfo info);

	/**
	 * Generate a string representation which can be parsed later
	 */
	String generate(ParameterInfo info, Object value);

	/**
	 * Parse the string representation of the parameter.
	 */
	ParameterValueProvider parse(ParameterInfo info, String urlPart);
}
