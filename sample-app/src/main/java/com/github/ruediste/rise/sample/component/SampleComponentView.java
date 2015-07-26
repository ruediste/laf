package com.github.ruediste.rise.sample.component;

import javax.inject.Inject;

import com.github.ruediste.rise.component.components.CButton;
import com.github.ruediste.rise.component.components.CPage;
import com.github.ruediste.rise.component.components.CTextField;
import com.github.ruediste.rise.component.tree.Component;
import com.github.ruediste.rise.sample.ViewComponent;
import com.github.ruediste1.i18n.lString.PatternString;
import com.github.ruediste1.i18n.label.Labeled;
import com.github.ruediste1.i18n.message.TMessage;
import com.github.ruediste1.i18n.message.TMessageUtil;

@Labeled
public class SampleComponentView extends
        ViewComponent<SampleComponentController> {

    @Inject
    TMessageUtil messageUtil;

    public interface Messages {
        @TMessage("The counter is {count}")
        PatternString theCounterIs(int count);
    }

    @Override
    protected Component createComponents() {
        return new CPage(label(this)).add(toComponent(html -> html
                .add(toComponentDirect(x -> x.write(messageUtil
                        .getMessageInterfaceInstance(Messages.class)
                        .theCounterIs(controller.counter))))
                .add(new CButton("ClickMe").setHandler(() -> controller.inc()))
                .add(new CTextField().bindText(() -> controller
                        .getData().getText()))));
    }
}
