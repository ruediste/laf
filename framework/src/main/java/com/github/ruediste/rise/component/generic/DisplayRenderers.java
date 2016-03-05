package com.github.ruediste.rise.component.generic;

import java.util.Optional;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.github.ruediste.c3java.properties.PropertyInfo;
import com.github.ruediste.c3java.properties.PropertyPath;
import com.github.ruediste.c3java.properties.PropertyUtil;
import com.github.ruediste.rise.core.strategy.Strategies;

@Singleton
public class DisplayRenderers {

	@Inject
	Strategies strategies;

	public class PropertyApi<T> {

		private PropertyInfo property;

		private PropertyApi(PropertyInfo property) {
			this.property = property;
		}

		public DisplayRenderer<T> get() {
			return tryGet().orElseThrow(() -> new RuntimeException("No display renderer found for " + property));
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Optional<DisplayRenderer<T>> tryGet() {
			return strategies.getStrategy(DisplayRendererFactory.class).element(property).cached(property)
					.get(f -> (Optional) f.getRenderer(property.getPropertyType(), Optional.of(property)));
		}
	}

	public PropertyApi<?> property(PropertyInfo info) {
		return new PropertyApi<>(info);
	}

	public <T, P> PropertyApi<P> property(Class<T> startClass, Function<T, P> propertyAccessor) {
		return new PropertyApi<>(
				PropertyUtil.getPropertyPath(startClass, x -> propertyAccessor.apply(x)).getAccessedProperty());
	}

	@SuppressWarnings("unchecked")
	public <T, P> PropertyApi<P> property(T start, Function<T, P> propertyAccessor) {
		PropertyPath propertyPath = PropertyUtil.getPropertyPath((Class<T>) start.getClass(),
				(T t) -> propertyAccessor.apply(t));
		return (PropertyApi<P>) property(propertyPath.getAccessedProperty());
	}

	public class TypeApi<T> {

		private Class<T> cls;

		private TypeApi(Class<T> cls) {
			this.cls = cls;
		}

		public DisplayRenderer<T> get() {
			return tryGet().orElseThrow(() -> new RuntimeException("No display renderer found for type " + cls));
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Optional<DisplayRenderer<T>> tryGet() {
			return (Optional) strategies.getStrategy(DisplayRendererFactory.class).cached(cls)
					.get(f -> f.getRenderer(cls, Optional.empty()));
		}
	}

	public <T> TypeApi<T> type(Class<T> cls) {
		return new TypeApi<>(cls);
	}

}