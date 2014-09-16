package laf.component.core;

import java.io.Serializable;

import javax.enterprise.inject.Typed;

import laf.component.core.pageScope.PageScoped;
import laf.core.persistence.LafPersistenceHolder;

@PageScoped
@Typed(PageScopedPersistenceHolder.class)
public class PageScopedPersistenceHolder extends LafPersistenceHolder implements
		Serializable {
	private static final long serialVersionUID = 1L;

}