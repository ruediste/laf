package laf.controllerInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import laf.*;
import laf.controllerInfo.impl.*;

import org.slf4j.Logger;

public class ControllerInfoRepositoryInitializer implements Runnable {

	@Inject
	Logger log;

	@Inject
	ControllerInfoRepository repository;

	@Inject
	BeanManager beanManager;

	@Override
	public void run() {
		Controller controllerAnnotation = new Controller() {
			@Override
			public Class<? extends Annotation> annotationType() {
				return Controller.class;
			}
		};

		EmbeddedController embeddedControllerAnnotation = new EmbeddedController() {

			@Override
			public Class<? extends Annotation> annotationType() {
				return EmbeddedController.class;
			}
		};

		for (Bean<?> bean : beanManager.getBeans(Object.class,
				controllerAnnotation)) {
			log.debug("Found controller " + bean.getBeanClass());
			repository.putControllerInfo(createControllerInfo(
					bean.getBeanClass(), false));
		}

		for (Bean<?> bean : beanManager.getBeans(Object.class,
				embeddedControllerAnnotation)) {
			log.debug("Found embedded controller " + bean.getBeanClass());
			repository.putControllerInfo(createControllerInfo(
					bean.getBeanClass(), true));
		}
	}

	public ControllerInfoImpl createControllerInfo(Class<?> controllerClass,
			boolean isEmbedded) {
		ControllerInfoImpl info = new ControllerInfoImpl(controllerClass,
				isEmbedded);
		for (Method method : info.getControllerClass().getMethods()) {
			if (method.getReturnType() == null) {
				continue;
			}

			if (!(ActionResult.class.isAssignableFrom(method.getReturnType()) || method
					.getReturnType().getAnnotation(EmbeddedController.class) != null)) {
				continue;
			}

			if (method.getDeclaringClass().getAnnotation(Controller.class) == null
					&& method.getDeclaringClass().getAnnotation(
							EmbeddedController.class) == null) {
				continue;
			}
			// create method info
			ActionMethodInfoImpl methodInfo = new ActionMethodInfoImpl(method);
			methodInfo.setControllerInfo(info);

			// calculate name
			methodInfo
					.setName(info.calculateUnusedMethodName(method.getName()));

			// add to repository
			info.putActionMethodInfo(methodInfo);

			// create parameter infos
			createParameterInfos(methodInfo);
		}
		return info;
	}

	private void createParameterInfos(ActionMethodInfoImpl actionMethodInfo) {
		for (Type type : actionMethodInfo.getMethod()
				.getGenericParameterTypes()) {
			ParameterInfoImpl parameterInfo = new ParameterInfoImpl(type);
			parameterInfo.setMethod(actionMethodInfo);
			actionMethodInfo.getParameters().add(parameterInfo);
		}
	}
}