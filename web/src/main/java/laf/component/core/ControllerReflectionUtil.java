package laf.component.core;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import laf.core.base.ActionResult;

public class ControllerReflectionUtil {

	public static boolean isActionMethod(Method method) {
		return Modifier.isPublic(method.getModifiers())
				&& (ActionResult.class.isAssignableFrom(method.getReturnType()));
	}

}
