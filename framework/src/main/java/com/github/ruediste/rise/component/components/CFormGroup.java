package com.github.ruediste.rise.component.components;

import java.util.Optional;

import com.github.ruediste.rise.component.tree.Component;
import com.github.ruediste.rise.component.tree.ComponentBase;
import com.github.ruediste1.i18n.lString.LString;

@DefaultTemplate(CFormGroupTemplate.class)
public class CFormGroup extends ComponentBase<CFormGroup> {

    private Optional<? extends LString> label = Optional.empty();
    private Component content;

    public CFormGroup(Runnable content) {
        this.content = new CRunnable(content);
    }

    public CFormGroup(Component content) {
        this.content = content;
    }

    public Optional<? extends LString> getLabel() {
        return label;
    }

    public CFormGroup setLabel(Optional<? extends LString> label) {
        this.label = label;
        return this;
    }

    public Component getContent() {
        return content;
    }
}
