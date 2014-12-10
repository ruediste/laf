package laf.component.web.components;

import laf.component.core.tree.*;

public class MultiChildrenComponent<TSelf extends ComponentBase<TSelf>> extends
ComponentBase<TSelf> {
	public final MultiChildrenRelation<Component, TSelf> children = new MultiChildrenRelation<>(
			self());

	public TSelf add(Component child) {
		return children.add(child);
	}
}