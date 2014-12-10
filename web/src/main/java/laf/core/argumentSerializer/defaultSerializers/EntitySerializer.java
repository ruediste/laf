package laf.core.argumentSerializer.defaultSerializers;

import java.lang.reflect.AnnotatedType;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Type;

import laf.core.argumentSerializer.ArgumentSerializer;
import laf.core.base.ReflectionUtil;
import laf.core.persistence.*;

import com.google.common.base.Supplier;

public class EntitySerializer implements ArgumentSerializer {

	@Inject
	BeanManager beans;

	@Inject
	LafPersistenceContextManager manager;

	private Iterable<IdentifierSerializer> identifierSerializers;

	public void initialize(Iterable<IdentifierSerializer> identifierSerializers) {
		this.identifierSerializers = identifierSerializers;
	}

	@Override
	public String generate(AnnotatedType type, Object value) {
		if (value == null) {
			return "null";
		}

		EntityManagerToken token = manager.getToken(ReflectionUtil
				.getQualifiers(type, beans));
		if (token == null) {
			return null;
		}

		EntityType<?> entity = manager.getEntity(token,
				(Class<?>) type.getType());
		if (entity == null) {
			return null;
		}

		Type<?> idType = entity.getIdType();

		Object identifier = manager.getPersistenceUnitUtil(token)
				.getIdentifier(value);

		for (IdentifierSerializer s : identifierSerializers) {
			String result = s.generate(idType, identifier);
			if (result != null) {
				return result;
			}
		}
		throw new RuntimeException("No entity id serializer found for type "
				+ idType + " used by entity of type "
				+ entity.getJavaType().getName());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Supplier<?> parse(AnnotatedType type, String urlPart) {
		if ("null".equals(urlPart)) {
			return () -> null;
		}

		EntityManagerToken token = manager.getToken(ReflectionUtil
				.getQualifiers(type, beans));
		if (token == null) {
			return null;
		}

		EntityType<?> entity = manager.getEntity(token,
				(Class<?>) type.getType());
		if (entity == null) {
			return null;
		}

		Type<?> idType = entity.getIdType();

		for (IdentifierSerializer s : identifierSerializers) {
			Supplier<?> idSupplier = s.parse(idType, urlPart);
			if (idSupplier != null) {
				return () -> {
					LafPersistenceHolder currentHolder = manager
							.getCurrentHolder();
					EntityManager entityManager = currentHolder
							.getEntityManager(token);
					Object id = idSupplier.get();
					return entityManager.find((Class) type.getType(), id);
				};
			}
		}
		throw new RuntimeException("No entity id serializer found for type "
				+ idType + " used by entity of type "
				+ entity.getJavaType().getName());

	}

}