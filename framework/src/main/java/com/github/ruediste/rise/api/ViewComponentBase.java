package com.github.ruediste.rise.api;

import com.github.ruediste.attachedProperties4J.AttachedPropertyBearerBase;
import com.github.ruediste.rise.component.tree.Component;

/**
 * Base class for view displaying {@link Component}s
 */
public abstract class ViewComponentBase<TController> extends
        AttachedPropertyBearerBase {

    protected TController controller;
    private Component rootComponent;

    public TController getController() {
        return controller;
    }

    /**
     * Initialize this view. To be called after instantiation and setting the
     * controller ({@link #setController(Object)})
     */
    public final void initialize(TController controller) {
        this.controller = controller;
        rootComponent = createComponents();
    }

    /**
     * Create the components of this view and return the root component. This
     * method is called after the instantiation of the view. The result is
     * written to {@link #rootComponent}.
     */
    abstract protected Component createComponents();

    /**
     * Return the root component of this view. The root component does not
     * change after initialization.
     */
    public Component getRootComponent() {
        return rootComponent;
    }

}