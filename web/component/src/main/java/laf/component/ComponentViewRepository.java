package laf.component;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import laf.base.Pair;

import com.google.common.reflect.TypeToken;

@ApplicationScoped
public class ComponentViewRepository {

	@Inject
	Instance<ComponentView<?>> componentViewInstance;

	@Inject
	BeanManager beanManager;

	Map<Pair<Class<?>, Class<? extends IViewQualifier>>, ViewEntry> viewMap = new HashMap<>();

	private static class ViewEntry {
		public Class<? extends ComponentView<?>> viewClass;
	}

	@SuppressWarnings("unchecked")
	@PostConstruct
	public void initialize() {

		for (Bean<?> bean : beanManager.getBeans(
				new TypeToken<ComponentView<?>>() {
					private static final long serialVersionUID = 1L;
				}.getType(), new Any() {

					@Override
					public Class<? extends Annotation> annotationType() {
						return Any.class;
					}
				})) {
			Class<?> controllerClass = TypeToken.of(bean.getBeanClass())
					.resolveType(ComponentView.class.getTypeParameters()[0])
					.getRawType();

			ViewEntry entry = new ViewEntry();
			entry.viewClass = (Class<? extends ComponentView<?>>) bean
					.getBeanClass();
			ViewQualifier viewQualifierAnnotation = bean.getBeanClass()
					.getAnnotation(ViewQualifier.class);
			Class<? extends IViewQualifier> qualifier = null;
			if (viewQualifierAnnotation != null) {
				qualifier = viewQualifierAnnotation.value();
			}
			ViewEntry existing = viewMap.put(
					new Pair<Class<?>, Class<? extends IViewQualifier>>(
							controllerClass, qualifier), entry);

			if (existing != null) {
				throw new RuntimeException("Two views found for controller "
						+ controllerClass.getName() + " and qualifier "
						+ qualifier == null ? "null" : qualifier.getName()
								+ ": " + entry.viewClass.getName() + ", "
								+ existing.viewClass.getName());
			}
		}
	}

	public <T> ComponentView<T> createView(Class<T> controllerClass) {
		return createView(controllerClass, null);
	}

	@SuppressWarnings("unchecked")
	public <T> ComponentView<T> createView(Class<T> controllerClass,
			Class<? extends IViewQualifier> qualifier) {
		// get the list of possible views
		ViewEntry entry = viewMap.get(Pair.create(controllerClass, qualifier));
		if (entry == null) {
			throw new RuntimeException(
					"There is no view for controller class "
							+ controllerClass.getName() + " and qualifier "
							+ qualifier == null ? "null" : qualifier.getName());
		}

		// create view instance
		return (ComponentView<T>) componentViewInstance.select(entry.viewClass)
				.get();
	}
}