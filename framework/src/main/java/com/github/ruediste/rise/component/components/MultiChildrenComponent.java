package com.github.ruediste.rise.component.components;

import com.github.ruediste.rise.component.tree.Component;
import com.github.ruediste.rise.component.tree.RelationsComponent;
import com.github.ruediste.rise.component.tree.MultiChildrenRelation;

public class MultiChildrenComponent<TSelf extends RelationsComponent<TSelf>>
        extends RelationsComponent<TSelf> {
    public final MultiChildrenRelation<Component, TSelf> children = new MultiChildrenRelation<>(
            self());

    public TSelf add(Component child) {
        return children.add(child);
    }
}
