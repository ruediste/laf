package laf.component.core.binding;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

import laf.component.core.binding.BindingExpressionExecutionLogManager.MethodInvocation;
import laf.core.base.Pair;

import com.google.common.base.CaseFormat;

public class PropertyUtil {

	public static String getProperty(List<MethodInvocation> path) {
		return path.stream().map(PropertyUtil::getProperty)
				.collect(Collectors.joining("."));
	}

	public static String getProperty(MethodInvocation invocation) {
		return getProperty(getPropertyInfo(invocation));
	}

	public static String getProperty(Pair<String, Boolean> info) {
		return info.getA();
	}

	public static boolean isGetter(MethodInvocation invocation) {
		return isGetter(getPropertyInfo(invocation));
	}

	public static boolean isGetter(Pair<String, Boolean> info) {
		return info.getB();
	}

	public static Pair<String, Boolean> getPropertyInfo(
			MethodInvocation invocation) {
		Method method = invocation.method;
		if (method.getName().equals("get")) {
			// get(String propertyName)
			if (method.getParameterCount() == 1
					&& String.class.equals(method.getParameterTypes()[0])) {
				return Pair.of((String) invocation.args[0], true);
			}

			// get(String propertyName, int index)
			if (method.getParameterCount() == 2
					&& String.class.equals(method.getParameterTypes()[0])
					&& Integer.TYPE.equals(method.getParameterTypes()[1])) {
				return Pair.of((String) invocation.args[0] + "["
						+ invocation.args[1] + "]", true);
			}

			// get(String propertyName, String key)
			if (method.getParameterCount() == 2
					&& String.class.equals(method.getParameterTypes()[0])
					&& String.class.equals(method.getParameterTypes()[1])) {
				return Pair.of((String) invocation.args[0] + "("
						+ invocation.args[1] + ")", true);
			}
		}

		if (method.getName().startsWith("get")) {
			String name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL,
					method.getName().substring("get".length()));

			// getFoo()
			if (method.getParameterCount() == 0) {
				return Pair.of(name, true);
			}

			// getFoo(1)
			if (method.getParameterCount() == 1
					&& Integer.TYPE.equals(method.getParameterTypes()[0])) {
				return Pair.of(name + "[" + invocation.args[0] + "]", true);
			}

		}

		if (method.getName().startsWith("set")) {
			String name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL,
					method.getName().substring("set".length()));
			// setFoo()
			if (method.getParameterCount() == 1) {
				return Pair.of(name, false);
			}

			// setFoo(1,x)
			if (method.getParameterCount() == 2
					&& Integer.TYPE.equals(method.getParameterTypes()[0])) {
				return Pair.of(name + "[" + invocation.args[0] + "]", false);
			}
		}

		if (method.getName().equals("set")) {
			// set(String propertyName, Object value)
			if (method.getParameterCount() == 2
					&& String.class.equals(method.getParameterTypes()[0])) {
				return Pair.of((String) invocation.args[0], false);
			}

			// set(String propertyName, int index, Object value)
			if (method.getParameterCount() == 3
					&& String.class.equals(method.getParameterTypes()[0])
					&& Integer.TYPE.equals(method.getParameterTypes()[1])) {
				return Pair.of((String) invocation.args[0] + "["
						+ invocation.args[1] + "]", false);
			}

			// get(String propertyName, String key)
			if (method.getParameterCount() == 3
					&& String.class.equals(method.getParameterTypes()[0])
					&& String.class.equals(method.getParameterTypes()[1])) {
				return Pair.of((String) invocation.args[0] + "("
						+ invocation.args[1] + ")", false);
			}
		}
		return null;
	}

}