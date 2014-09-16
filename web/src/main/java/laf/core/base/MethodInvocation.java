package laf.core.base;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

import laf.mvc.core.ActionPath;

import com.google.common.base.Objects;

/**
 * An invocation of an method
 */
public class MethodInvocation<T> {
	/**
	 * When comparing two {@link ActionPath}s with different argument
	 * representations using
	 * {@link ActionPath#isCallToSameActionMethod(ActionPath, ParameterValueComparator)}
	 * , providing an implementation of this interface allows to specify a
	 * strategy to compare the arguments.
	 */
	public interface ParameterValueComparator<A, B> {
		public boolean equals(A a, B b);
	}

	private final List<T> arguments = new ArrayList<>();
	final private Class<?> instanceClass;
	final private Method method;

	public MethodInvocation(Class<?> instanceClass, Method method) {
		this.instanceClass = instanceClass;
		this.method = method;

	}

	/**
	 * Create a copy of this invocation, not including the argument list
	 */
	public MethodInvocation(MethodInvocation<?> invocation) {
		instanceClass = invocation.instanceClass;
		method = invocation.method;
	}

	public List<T> getArguments() {
		return arguments;
	}

	public <O> boolean isCallToSameMethod(
			MethodInvocation<O> other,
			MethodInvocation.ParameterValueComparator<? super T, ? super O> comparator) {
		if (method != other.method) {
			return false;
		}
		if (arguments.size() != other.getArguments().size()) {
			return false;
		}

		Iterator<T> it = arguments.iterator();
		Iterator<O> oit = other.getArguments().iterator();
		while (it.hasNext() && oit.hasNext()) {
			if (!comparator.equals(it.next(), oit.next())) {
				return false;
			}
		}

		return true;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("method", method)
				.add("arguments", arguments).toString();
	}

	public Method getMethod() {
		return method;
	}

	public Class<?> getInstanceClass() {
		return instanceClass;
	}

	public <R> MethodInvocation<R> map(Function2<Type, ? super T, R> func) {
		MethodInvocation<R> invocation = new MethodInvocation<>(this);
		Iterator<Type> pit = Arrays.asList(
				getMethod().getGenericParameterTypes()).iterator();
		Iterator<T> ait = getArguments().iterator();
		while (pit.hasNext() && ait.hasNext()) {
			invocation.getArguments().add(func.apply(pit.next(), ait.next()));
		}
		return invocation;
	}
}