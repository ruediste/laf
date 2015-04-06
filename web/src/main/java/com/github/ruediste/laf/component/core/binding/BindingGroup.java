package com.github.ruediste.laf.component.core.binding;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

import net.sf.cglib.proxy.*;

import com.github.ruediste.laf.component.core.binding.BindingExpressionExecutionLogManager.MethodInvocation;
import com.github.ruediste.laf.component.core.tree.ComponentBase;
import com.github.ruediste.laf.core.base.attachedProperties.AttachedProperty;
import com.github.ruediste.laf.core.base.attachedProperties.AttachedPropertyBearer;
import com.google.common.base.Defaults;
import com.google.common.reflect.TypeToken;

/**
 * A group of bindings managed by a controller.
 *
 * <p>
 * Data binding is used to move data between the domain objects and the view
 * components. Properties are bound together in a typesafe manner using lambda
 * expressions.
 * </p>
 *
 * <p>
 * <img src="doc-files/overview.png" />
 * </p>
 *
 * <p>
 * The binding is triggered explicitly. The controller instantiates a
 * {@link BindingGroup} and references it in an instance variable. If the data
 * is ready, the controller uses the {@link #pullUp(Object)} method to fill the
 * components with data. To retrieve the data from the components, it uses
 * {@link #pushDown(Object)}. To the view the binding group is exposed via a
 * function returning {@link #proxy()}.
 * </p>
 *
 * <p>
 * The component classes have a
 * {@link ComponentBase#bind(java.util.function.Consumer)} method which accepts
 * a lambda expression. The lambda expression has the component as parameter and
 * sets a property of the component to some value retrieved via a binding group
 * exposed by the controller.
 * </p>
 *
 * <p>
 * While establishing the binding, the lambda expression is called with a
 * dynamic proxy of the component class as parameter.
 * {@link BindingGroup#proxy()} returns a dynamic proxy. The two dynamic proxies
 * record the methods invoked on themselves. This information is used to
 * determine which properties are accessed.
 * </p>
 *
 * <p>
 * When the involved properties are known, it is determined if they are readable
 * and writeable. If both are, a two-way binding is set up. Otherwise an
 * appropriate one-way binding is used.
 * </p>
 *
 * <p>
 * When the binding is established, the lambda expression is not called again.
 * The properties are read and written using reflection. Thus any processing
 * logic contained in the expression does not affect the binding.
 * </p>
 *
 * <p>
 * <strong> One Way Bindings </strong><br/>
 * Using {@link ComponentBase#bindOneWay(java.util.function.Consumer)} the check
 * if a binding could be two way is suppressed and the binding always takes the
 * direction as specified by the lambda expression.
 * </p>
 *
 * <p>
 * <strong> Explicit Bindings </strong><br/>
 * Using
 * {@link ComponentBase#bind(java.util.function.Consumer, java.util.function.Consumer)}
 * an explicit binding is established. The two lambda expressions are always
 * invoked with the real objects. This can be used to easily define
 * one-of-a-kind transformations in place. Either expression can be null, in
 * which case it is ignored.
 * </p>
 *
 * <p>
 * <strong> Transformers </strong><br/>
 * Transformers allow a value to be transformed. When a lambda expression is
 * executed to establish a binding, the usage of any transformer is recorded and
 * integrated in the binding. Transformers can be one-way (implementing only
 * {@link BindingTransformer}) or two-way (implementing
 * {@link TwoWayBindingTransformer}). If it is attempted to establish a two-way
 * binding using an one-way transformer, an exception is raised.
 * </p>
 */
public class BindingGroup<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	private final Class<?> tClass;

	private final AttachedProperty<AttachedPropertyBearer, Set<Binding<T>>> bindings = new AttachedProperty<>();

	WeakHashMap<AttachedPropertyBearer, Object> components = new WeakHashMap<>();

	T data;

	public BindingGroup(Class<T> cls) {
		tClass = cls;
	}

	public BindingGroup(TypeToken<T> token) {
		tClass = token.getRawType();
	}

	public Stream<Binding<T>> getBindings() {
		return components.keySet().stream()
				.flatMap(c -> bindings.get(c).stream());
	}

	public void pullUp() {
		getBindings().filter(b -> b.getPullUp() != null).forEach(
				b -> b.getPullUp().accept(data));
	}

	public void pushDown() {
		getBindings().filter(b -> b.getPushDown() != null).forEach(
				b -> b.getPushDown().accept(data));
	}

	public T get() {
		return data;
	}

	public void set(T data) {
		this.data = data;
	}

	/**
	 * While evaluating a binding expression, return a recording proxy.
	 * Otherwise return {@link #data}.
	 */
	public T proxy() {
		BindingExpressionExecutionLog log = BindingExpressionExecutionLogManager
				.getCurrentLog();
		if (log == null) {
			return data;
		}
		log.involvedBindingGroup = this;
		return createModelProxy(tClass);
	}

	@SuppressWarnings("unchecked")
	private <TModel> TModel createModelProxy(Class<?> modelClass) {
		Enhancer e = new Enhancer();
		e.setSuperclass(modelClass);
		e.setCallback(new MethodInterceptor() {

			@Override
			public Object intercept(Object obj, Method method, Object[] args,
					MethodProxy proxy) throws Throwable {
				BindingExpressionExecutionLog info = BindingExpressionExecutionLogManager
						.getCurrentLog();
				info.modelPath.add(new MethodInvocation(method, args));

				Class<?> returnType = method.getReturnType();
				if (isTerminal(returnType)) {
					return Defaults.defaultValue(returnType);
				}
				return createModelProxy(returnType);
			}

		});

		return (TModel) e.create();
	}

	private boolean isTerminal(Class<?> clazz) {
		return clazz.isPrimitive() || String.class.equals(clazz)
				|| Date.class.equals(clazz);
	}

	/**
	 * Create a dummy usable for use with beanutils
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	T createDummyProxy() {
		return (T) createDummyProxy(tClass);
	}

	private Object createDummyProxy(Class<?> cls) {
		Enhancer e = new Enhancer();
		e.setSuperclass(cls);
		e.setCallback(new MethodInterceptor() {

			@Override
			public Object intercept(Object obj, Method method, Object[] args,
					MethodProxy proxy) throws Throwable {
				if (isTerminal(method.getReturnType())) {
					return Defaults.defaultValue(method.getReturnType());
				}
				return createDummyProxy(method.getReturnType());
			}
		});

		return e.create();
	}

	@SuppressWarnings({ "unchecked" })
	void addBindingUntyped(Binding<?> binding) {
		addBinding((Binding<T>) binding);
	}

	void addBinding(Binding<T> binding) {
		AttachedPropertyBearer component = binding.getComponent();
		Set<Binding<T>> set = bindings.get(component);
		if (set == null) {
			set = new HashSet<>();
			bindings.set(component, set);
			components.put(component, null);
		}

		set.add(binding);

		if (binding.getPullUp() != null) {
			binding.getPullUp().accept(data);
		}
	}
}