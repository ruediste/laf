package com.github.ruediste.laf.core.persistence;

import java.lang.annotation.Annotation;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;

import net.sf.cglib.proxy.Dispatcher;
import net.sf.cglib.proxy.Enhancer;

import com.github.ruediste.laf.core.persistence.em.EntityManagerHolder;
import com.github.ruediste.salta.jsr330.AbstractModule;
import com.github.ruediste.salta.jsr330.Injector;
import com.github.ruediste.salta.jsr330.Provides;

public class PersistenceDynamicModule extends AbstractModule {

	private Injector permanentInjector;

	public PersistenceDynamicModule(Injector permanentInjector) {
		this.permanentInjector = permanentInjector;
	}

	@Override
	protected void configure() throws Exception {

		// register DBLinks
		DataBaseLinkRegistry registry = permanentInjector
				.getInstance(DataBaseLinkRegistry.class);
		for (DataBaseLink link : registry.getLinks()) {
			// bind data source
			bind(DataSource.class).annotatedWith(link.getQualifier())
					.toProvider(() -> link.getDataSource()).in(Singleton.class);

			// create EMF and bind
			{
				EntityManagerFactory emf = link.createEntityManagerFactory();
				bind(EntityManagerFactory.class).annotatedWith(
						link.getQualifier()).toProvider(() -> emf);
			}

			Class<? extends Annotation> requiredQualifier = link.getQualifier();

			bind(EntityManager.class).annotatedWith(requiredQualifier)
					.toProvider(new Provider<EntityManager>() {
						@Inject
						EntityManagerHolder holder;

						@Override
						public EntityManager get() {
							return (EntityManager) Enhancer.create(
									EntityManager.class, new Dispatcher() {

										@Override
										public Object loadObject()
												throws Exception {
											return holder
													.getEntityManager(requiredQualifier);
										}
									});

						}
					}).in(Singleton.class);
		}
	}

	@Provides
	TransactionManager transactionManager() {
		return permanentInjector.getInstance(TransactionManager.class);
	}

	@Provides
	TransactionSynchronizationRegistry transactionSynchronizationRegistry() {
		return permanentInjector
				.getInstance(TransactionSynchronizationRegistry.class);
	}

	@Provides
	TransactionProperties transactionProperties() {
		return permanentInjector.getInstance(TransactionProperties.class);
	}
}
