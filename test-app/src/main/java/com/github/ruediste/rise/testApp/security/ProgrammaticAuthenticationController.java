package com.github.ruediste.rise.testApp.security;

import javax.inject.Inject;

import com.github.ruediste.rise.api.ControllerMvc;
import com.github.ruediste.rise.core.ActionResult;
import com.github.ruediste.rise.core.CoreRequestInfo;
import com.github.ruediste.rise.core.security.AuthenticationHolder;
import com.github.ruediste.rise.testApp.TestCanvas;
import com.github.ruediste.rise.testApp.TestPageTemplate;
import com.github.ruediste.rise.testApp.ViewMvc;
import com.github.ruediste1.i18n.label.Labeled;

public class ProgrammaticAuthenticationController extends
        ControllerMvc<ProgrammaticAuthenticationController> {

    @Inject
    CoreRequestInfo info;

    @Labeled
    public static class View extends
            ViewMvc<ProgrammaticAuthenticationController, String> {

        @Inject
        TestPageTemplate template;

        @Override
        protected void renderContent(TestCanvas html) {
            html.write(data());
        }

    }

    @Inject
    AuthenticationHolder authenticationHolder;

    public ActionResult noAuthenticationRequired() {
        return view(View.class, "success");
    }

    public ActionResult authenticationRequired() {
        authenticationHolder.checkAutheticationPresetn();
        return view(View.class, "success");
    }

}
