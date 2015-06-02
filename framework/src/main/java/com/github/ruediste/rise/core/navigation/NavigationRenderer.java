package com.github.ruediste.rise.core.navigation;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Provider;

import com.github.ruediste.rendersnakeXT.canvas.Renderable;
import com.github.ruediste.rise.core.navigation.Navigation.NavigationItem;
import com.github.ruediste.rise.integration.BootstrapRiseCanvas;

public class NavigationRenderer {

    @Inject
    Provider<NavigationItemSelectionCache> cache;

    public static class NavigationRendererOptions {
        public boolean isInverted;

        public Optional<Runnable> brandRenderer;

        public boolean isInverted() {
            return isInverted;
        }

        public NavigationRendererOptions setInverted(boolean isInverted) {
            this.isInverted = isInverted;
            return this;
        }

        public Optional<Runnable> getBrandRenderer() {
            return brandRenderer;
        }

        public NavigationRendererOptions setBrandRenderer(Runnable brandRenderer) {
            this.brandRenderer = Optional.of(brandRenderer);
            return this;
        }

    }

    private static class Ctx {
        NavigationItemSelectionCache cache;

        public NavigationRendererOptions opts;
    }

    public Renderable<BootstrapRiseCanvas<?>> side(Navigation nav,
            NavigationRendererOptions opts) {
        return html -> {
            Ctx ctx = new Ctx();
            ctx.opts = opts;
            ctx.cache = cache.get();

            html.ul().CLASS("nav nav-pills nav-stacked");
            renderSideItems(html, ctx, nav.getRootItems());
            html._ul();
        };
    }

    private void renderSideItems(BootstrapRiseCanvas<?> html, Ctx ctx,
            List<NavigationItem> items) {
        for (NavigationItem item : items) {
            html.li().CLASS("").ROLE("presentation");
            boolean selected = ctx.cache.isSelected(item);
            if (selected)
                html.CLASS("active");
            if (item.getChildren().isEmpty()) {
                if (item.target.isPresent())
                    html.a().HREF(item.target.get()).content(item.text);
                else
                    html.a().HREF("#").content(item.text);
                html._li();
            } else {
                html.a().HREF("#").CLASS("rise-tree-toggler")
                        .content(item.text).ul()
                        .CLASS("nav nav-pills nav-stacked rise-tree");
                if (!selected)
                    html.STYLE("display: none;");
                renderSideItems(html, ctx, item.getChildren());
                html._ul();
                html._li();
            }
        }
    }

    public Renderable<BootstrapRiseCanvas<?>> navbar(Navigation nav, String id,
            Consumer<NavigationRendererOptions> optsConsumer) {
        //@formatter:off
        return html -> {
            NavigationRendererOptions opts = new NavigationRendererOptions();
            optsConsumer.accept(opts);
            Ctx ctx = new Ctx();
            ctx.opts = opts;
            ctx.cache = cache.get();
            html.div().CLASS("navbar navbar-default")
                .bContainer_fluid()
                    .div().CLASS("navbar-header")
                        .button().TYPE("button").CLASS("navbar-toggle collapsed").DATA("toggle", "collapse").DATA("target", "#"+id)
                            .span().CLASS("sr-only").content("Toggle navigation")
                            .span().CLASS("icon-bar")._span()
                            .span().CLASS("icon-bar")._span()
                            .span().CLASS("icon-bar")._span()
                            .span().CLASS("icon-bar")._span()
                        ._button()
                        .fIfPresent(opts.getBrandRenderer(), x->html.a().CLASS("navbar-brand").render(x))
                        ._div()
                    .div().CLASS("collapse navbar-collapse").ID(id)
                        .ul().CLASS("nav navbar-nav");
                            renderNavItems(html, ctx, nav.getRootItems(),1);
                        html._ul();
                    html._div()
                ._bContainer_fluid()
            ._div();

        };
        //@formatter:on
    }

    private void renderNavItems(BootstrapRiseCanvas<?> html, Ctx ctx,
            Iterable<NavigationItem> items, int level) {

        for (NavigationItem item : items) {
            html.li();
            if (level == 1 && ctx.cache.isSelected(item))
                html.CLASS("active");

            if (item.getChildren().isEmpty()) {
                if (item.target.isPresent())
                    html.a().HREF(item.target.get()).content(item.text);
                else
                    html.a().HREF("#").content(item.text);
                html._li();
            } else {
                html.CLASS("dropdown").a().HREF("#").CLASS("dropdown-toggle")
                        .DATA("toggle", "dropdown").ROLE("button")
                        .ARIA_EXPANDED("false").write(item.text).bCaret()._a();
                html.ul().CLASS("dropdown-menu").ROLE("menu");
                renderNavItems(html, ctx, item.getChildren(), level + 1);
                html._ul()._li();
            }

        }

    }
}