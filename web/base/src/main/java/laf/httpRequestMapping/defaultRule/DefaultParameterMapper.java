package laf.httpRequestMapping.defaultRule;

import javax.inject.Inject;

import laf.actionPath.ActionPath;
import laf.base.Function2;
import laf.controllerInfo.ParameterInfo;
import laf.httpRequestMapping.parameterHandler.ParameterHandlerModule;
import laf.httpRequestMapping.parameterHandler.ParameterHandlerRepository;
import laf.httpRequestMapping.parameterValueProvider.ParameterValueProvider;
import laf.httpRequestMapping.twoStageMappingRule.ParameterMapper;

/**
 * A {@link ParameterMapper} using the {@link ParameterHandlerModule}
 * infrastructure.
 */
public class DefaultParameterMapper implements ParameterMapper {

	@Inject
	ParameterHandlerRepository handlerRepository;

	@Override
	public ActionPath<ParameterValueProvider> parse(
			final ActionPath<String> path) {
		return path
				.mapWithParameter(new Function2<ParameterInfo, String, ParameterValueProvider>() {

					@Override
					public ParameterValueProvider apply(ParameterInfo a,
							String b) {
						return handlerRepository.getParameterHandler(a).parse(
								a, b);
					}
				});
	}

	@Override
	public ActionPath<String> generate(final ActionPath<Object> path) {
		return path
				.mapWithParameter(new Function2<ParameterInfo, Object, String>() {

					@Override
					public String apply(ParameterInfo a, Object b) {
						return handlerRepository.getParameterHandler(a)
								.generate(a, b);
					}
				});
	}
}