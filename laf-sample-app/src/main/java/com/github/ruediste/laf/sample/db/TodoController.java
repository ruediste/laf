package com.github.ruediste.laf.sample.db;

import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.github.ruediste.laf.api.ControllerMvcWeb;
import com.github.ruediste.laf.core.ActionResult;
import com.github.ruediste.laf.core.CoreRequestInfo;
import com.github.ruediste.laf.mvc.Updating;

public class TodoController extends ControllerMvcWeb<TodoController> {

	static class IndexData {
		public List<TodoItem> allItems;
	}

	@Inject
	EntityManager em;

	@Inject
	CoreRequestInfo info;

	public ActionResult index() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<TodoItem> query = cb.createQuery(TodoItem.class);
		Root<TodoItem> from = query.from(TodoItem.class);
		query.select(from);
		List<TodoItem> allItems = em.createQuery(query).getResultList();
		IndexData data = new IndexData();
		data.allItems = allItems;
		return view(IndexView.class, data);
	}

	@Updating
	public ActionResult delete(TodoItem item) {
		em.remove(item);
		commit();
		return redirect(go().index());
	}

	@Updating
	public ActionResult add() {
		String name = info.getRequest().getParameter("name");
		TodoItem item = new TodoItem();
		item.setName(name);
		em.persist(item);
		commit();
		return redirect(go().index());
	}
}
