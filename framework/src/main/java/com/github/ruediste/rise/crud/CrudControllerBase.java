package com.github.ruediste.rise.crud;

import java.lang.annotation.Annotation;

import javax.inject.Inject;

import com.github.ruediste.rendersnakeXT.canvas.Glyphicon;
import com.github.ruediste.rise.api.ControllerComponent;
import com.github.ruediste.rise.component.binding.BindingGroup;
import com.github.ruediste.rise.core.ActionResult;
import com.github.ruediste.rise.crud.CrudUtil.BrowserFactory;
import com.github.ruediste.rise.crud.CrudUtil.DisplayFactory;
import com.github.ruediste.rise.integration.GlyphiconIcon;
import com.github.ruediste1.i18n.label.Labeled;

public abstract class CrudControllerBase extends ControllerComponent {

    @Inject
    CrudUtil crudUtil;

    public static class Data {
        private Object subController;

        public Object getSubController() {
            return subController;
        }

        public void setSubController(Object subController) {
            this.subController = subController;
        }
    }

    private BindingGroup<Data> data = new BindingGroup<CrudControllerBase.Data>(
            new Data());

    public Data data() {
        return data.proxy();
    }

    @Labeled
    @GlyphiconIcon(Glyphicon.list)
    public ActionResult browse(Class<?> entityClass,
            Class<? extends Annotation> emQualifier) {
        data.get().setSubController(
                crudUtil.getStrategy(BrowserFactory.class, entityClass)
                        .createBrowser(entityClass, emQualifier));
        return null;
    }

    @Labeled
    @GlyphiconIcon(Glyphicon.eye_open)
    public ActionResult display(Object entity) {
        data.get().setSubController(
                crudUtil.getStrategy(DisplayFactory.class, entity.getClass())
                        .createDisplay(entity));
        return null;
    }
}
