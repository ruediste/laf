package com.github.ruediste.rise.component.components;

import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.github.ruediste.c3java.invocationRecording.MethodInvocationRecorder;
import com.github.ruediste.rise.component.tree.Component;
import com.github.ruediste.rise.component.tree.ComponentBase;
import com.github.ruediste.rise.core.ActionResult;
import com.github.ruediste.rise.core.actionInvocation.ActionInvocationResult;

/**
 * A button triggering a handler on the server. If a handler is specified, it
 * will be called using a page reload. If a target is present, the button will
 * work as link.
 * 
 * <p>
 * If no children are present, the handler will be used to determine the invoked
 * proxy method on the controller. The label (mandatory) and icon (optional)
 * present on that method are shown.
 */
@DefaultTemplate(CButtonTemplate.class)
public class CButton extends ComponentBase<CButton> {
    private Runnable handler;
    private ActionResult target;
    private Method invokedMethod;
    private boolean isDisabled;
    private Component body;

    public CButton() {
    }

    public CButton(String text) {
        this.body = new CText(text);
    }

    public CButton(Runnable body) {
        this.body = new CRunnable(body);
    }

    /**
     * When the button is clicked, the handler will be called with the target as
     * argument. A {@link CIconLabel} is added as child, using the invoked
     * method to obtain a label and an (optional) icon. In addition, the
     * {@link #TEST_NAME(String)} is set to the name of the method.
     */
    public <T> CButton(T target, Consumer<T> handler) {
        this(target, handler, false);
    }

    /**
     * When the button is clicked, the handler will be called with the target as
     * argument. A {@link CIconLabel} is added as child, using the invoked
     * method to obtain a label and an (optional) icon. In addition, the
     * {@link #TEST_NAME(String)} is set to the name of the method.
     */
    public <T> CButton(T target, BiConsumer<CButton, T> handler) {
        this(target, handler, false);
    }

    /**
     * When the button is clicked, the handler will be called with the target as
     * argument. A {@link CIconLabel} is added as child, using the invoked
     * method to obtain a label and an (optional) icon. In addition, the
     * {@link #TEST_NAME(String)} is set to the name of the method.
     */
    public <T> CButton(T target, Consumer<T> handler, boolean showIconOnly) {
        this(target, (btn, t) -> handler.accept(t), showIconOnly);
    }

    /**
     * Create a button which will link directly to the specified target (without
     * causing a request to the containing page). A {@link CIconLabel} is added
     * as child, using the invoked action method to obtain a label and an
     * (optional) icon. In addition, the {@link #TEST_NAME(String)} is set to
     * the name of the method.
     */
    public <T> CButton(ActionResult target) {
        this(target, false);
    }

    /**
     * Create a button which will link directly to the specified target (without
     * causing a request to the containing page). A {@link CIconLabel} is added
     * as child, using the invoked action method to obtain a label and an
     * (optional) icon. In addition, the {@link #TEST_NAME(String)} is set to
     * the name of the method.
     */
    public <T> CButton(ActionResult target, boolean showIconOnly) {
        this.setTarget(target);
        setTarget(target);
        if (invokedMethod != null)
            TEST_NAME(invokedMethod.getName());
        body = new CIconLabel().setMethod(invokedMethod).setShowIconOnly(showIconOnly);
    }

    /**
     * When the button is clicked, the handler will be called with the target as
     * argument. A {@link CIconLabel} is added as child, using the invoked
     * method to obtain a label and an (optional) icon. In addition, the
     * {@link #TEST_NAME(String)} is set to the name of the method.
     */
    @SuppressWarnings("unchecked")
    public <T> CButton(T target, BiConsumer<CButton, T> handler, boolean showIconOnly) {
        this.handler = () -> handler.accept(this, target);
        invokedMethod = MethodInvocationRecorder
                .getLastInvocation((Class<T>) target.getClass(), t -> handler.accept(this, t)).getMethod();
        TEST_NAME(invokedMethod.getName());
        body = new CIconLabel().setMethod(invokedMethod).setShowIconOnly(showIconOnly);
    }

    public CButton setHandler(Runnable handler) {
        if (target != null && handler != null)
            throw new IllegalStateException("Cannot set handler if the target is set. Clear target first");

        this.handler = handler;
        return this;
    }

    public Runnable getHandler() {
        return handler;
    }

    /**
     * Get the target of this button. When the button is clicked, not page
     * reload is triggered.
     */
    public ActionResult getTarget() {
        return target;
    }

    /**
     * Set the target of this button. No page request will be triggered.
     */
    public CButton setTarget(ActionResult target) {
        if (target != null && handler != null)
            throw new IllegalStateException("Cannot set target if the handler is set. Clear handler first");
        this.target = target;
        if (target != null)
            invokedMethod = ((ActionInvocationResult) target).methodInvocation.getMethod();
        return this;
    }

    @Override
    public boolean isDisabled() {
        return isDisabled;
    }

    public CButton setDisabled(boolean isDisabled) {
        this.isDisabled = isDisabled;
        return this;
    }

    /**
     * Method beeing invoked. Can be null. Can be present both if a handler or a
     * target is defined.
     */
    public Method getInvokedMethod() {
        return invokedMethod;
    }

    public void setInvokedMethod(Method invokedMethod) {
        this.invokedMethod = invokedMethod;
    }

    public Component getBody() {
        return body;
    }
}
