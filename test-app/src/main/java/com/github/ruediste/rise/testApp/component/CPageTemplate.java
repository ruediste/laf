package com.github.ruediste.rise.testApp.component;

import javax.inject.Inject;

import com.github.ruediste.rise.component.components.CPage;
import com.github.ruediste.rise.component.components.ComponentTemplateBase;
import com.github.ruediste.rise.component.tree.Component;
import com.github.ruediste.rise.core.web.CoreAssetBundle;
import com.github.ruediste.rise.core.web.assetPipeline.AssetBundle;
import com.github.ruediste.rise.core.web.assetPipeline.AssetBundleOutput;
import com.github.ruediste.rise.core.web.bootstrap.BootstrapBundle;
import com.github.ruediste.rise.core.web.jQuery.JQueryAssetBundle;
import com.github.ruediste.rise.core.web.jQueryUi.JQueryUiAssetBundle;
import com.github.ruediste.rise.integration.RiseCanvas;
import com.github.ruediste.rise.integration.RisePageTemplate;
import com.github.ruediste.rise.integration.RisePageTemplate.RisePageTemplateParameters;
import com.github.ruediste.rise.testApp.TestCanvas;

public class CPageTemplate extends ComponentTemplateBase<CPage> {

    private static class Bundle extends AssetBundle {
        @Inject
        CoreAssetBundle coreBundle;

        @Inject
        BootstrapBundle bootstrapBundle;

        @Inject
        JQueryAssetBundle jQueryAssetBundle;

        @Inject
        JQueryUiAssetBundle jQueryUiAssetBundle;

        AssetBundleOutput out = new AssetBundleOutput(this);

        @Override
        public void initialize() {
            jQueryAssetBundle.out.send(out);
            jQueryUiAssetBundle.out.send(out);
            bootstrapBundle.out.send(out);
            coreBundle.out.send(out);
        }

    }

    @Inject
    Bundle bundle;
    @Inject
    RisePageTemplate<TestCanvas> renderer;

    @Override
    public void doRender(CPage component, RiseCanvas<?> html) {
        doRender(component, (TestCanvas) html);
    }

    public void doRender(CPage component, TestCanvas html) {
        renderer.renderOn(html, new RisePageTemplateParameters<TestCanvas>() {

            @Override
            protected void renderJsLinks(TestCanvas html) {
                html.rJsLinks(bundle.out);

            }

            @Override
            protected void renderHead(TestCanvas html) {

            }

            @Override
            protected void renderCssLinks(TestCanvas html) {
                html.rCssLinks(bundle.out);
            }

            @Override
            protected void renderBody(TestCanvas html) {
                for (Component child : component.getChildren())
                    util.render(child, html);
            }
        });
    }
}
