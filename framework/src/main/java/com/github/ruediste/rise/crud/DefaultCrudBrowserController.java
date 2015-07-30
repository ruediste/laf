package com.github.ruediste.rise.crud;

import static java.util.stream.Collectors.toList;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;

import com.github.ruediste.c3java.properties.PropertyInfo;
import com.github.ruediste.rendersnakeXT.canvas.Glyphicon;
import com.github.ruediste.rise.api.SubControllerComponent;
import com.github.ruediste.rise.component.ComponentUtil;
import com.github.ruediste.rise.component.binding.BindingGroup;
import com.github.ruediste.rise.component.components.CButton;
import com.github.ruediste.rise.component.components.CButtonTemplate;
import com.github.ruediste.rise.component.components.CDataGrid;
import com.github.ruediste.rise.component.components.CDataGrid.Cell;
import com.github.ruediste.rise.component.components.CDataGrid.Column;
import com.github.ruediste.rise.component.components.CText;
import com.github.ruediste.rise.component.tree.Component;
import com.github.ruediste.rise.core.persistence.PersistentTypeIdentifier;
import com.github.ruediste.rise.core.persistence.em.EntityManagerHolder;
import com.github.ruediste.rise.crud.CrudPropertyFilter.CrudFilterPersitenceContext;
import com.github.ruediste.rise.crud.CrudUtil.CrudPicker;
import com.github.ruediste.rise.integration.GlyphiconIcon;
import com.github.ruediste.rise.util.GenericEvent;
import com.github.ruediste1.i18n.lString.LString;
import com.github.ruediste1.i18n.label.LabelUtil;
import com.github.ruediste1.i18n.label.Labeled;
import com.github.ruediste1.i18n.message.TMessage;
import com.github.ruediste1.i18n.message.TMessages;
import com.google.common.base.Preconditions;

public class DefaultCrudBrowserController<T> extends SubControllerComponent
        implements CrudPicker {
    @Inject
    CrudReflectionUtil crudReflectionUtil;

    @Inject
    CrudPropertyFilters filters;

    @Inject
    ComponentUtil componentUtil;

    private enum Mode {
        BROWSER, PICKER
    }

    @SuppressWarnings("unused")
    private static class DefaultCrudBrowserView<T> extends
            DefaultCrudViewComponent<DefaultCrudBrowserController<T>> {

        @TMessages
        public interface Messages {

            @TMessage("Filter")
            LString filter();

            @TMessage("Actions")
            LString actions();

            @TMessage("Browser for {clazz}")
            LString browserFor(String clazz);

            @TMessage("Picker for {clazz}")
            LString pickerFor(String clazz);
        }

        @Inject
        CrudReflectionUtil crudReflectionUtil;

        @Inject
        Messages messages;

        @Inject
        LabelUtil labelUtil;

        @Override
        protected Component createComponents() {

            // create columns
            ArrayList<Column<T>> columns = new ArrayList<>();
            for (Attribute<?, ?> p : controller.properties) {
                PropertyInfo property = crudReflectionUtil.getProperty(p);
                columns.add(new Column<>(() -> new CDataGrid.Cell(labelUtil
                        .getPropertyLabel(property)), item -> new Cell(
                        new CText(Objects.toString(property.getValue(item))))));
            }
//@formatter:off
            Function<T, Cell> actionsFactory;
            if (controller.mode==Mode.BROWSER) {
                actionsFactory = item -> new Cell(toComponent(html -> html
                    .add(new CButton(go(CrudControllerBase.class).display(item), true)
                      .apply(CButtonTemplate.setArgs(x -> x.primary())))
                    .add(new CButton(go(CrudControllerBase.class).edit(item), true)
                      .apply(CButtonTemplate.setArgs(x -> {})))
                    .add(new CButton(go(CrudControllerBase.class).delete(item), true)
                      .apply(CButtonTemplate.setArgs(x -> x.danger())))
                      ));
            } else {
                actionsFactory = item -> new Cell(toComponent(html -> html
                        .add(new CButton(controller,c->c.pick(item), true)
                        .apply(CButtonTemplate.setArgs(x -> x.primary())))
                        ));
            }
                
            columns.add(new Column<T>(
                    () -> new Cell(new CText(messages.actions())),
                    actionsFactory));
            return toComponent(html -> html
                    .h1().content(controller.mode==Mode.BROWSER?
                      messages.browserFor(controller.entityClass.getName())
                      :messages.pickerFor(controller.entityClass.getName()))
                    .div().CLASS("panel panel-default")
                      .div().CLASS("panel-heading")
                        .content(messages.filter())
                      .div().CLASS("panel-body")
                        .fForEach(controller.filterList, filter -> {
                          html.add(filter.getComponent());
                        })
                      ._div()
                    ._div()
                    .add(new CButton(controller, x -> x.search()).apply(CButtonTemplate.setArgs(x->x.primary())))
                    .fIf(controller.mode==Mode.BROWSER, 
                      ()->html.add(new CButton(go(CrudControllerBase.class).create(controller.entityClass, controller.emQualifier)))
                    )
                    .add(new CDataGrid<T>().setColumns(columns).bindOneWay(
                            g -> g.setItems(controller.data().getItems())))
                    .fIf(controller.mode==Mode.PICKER,()->html
                      .add(new CButton(controller, c -> c.cancel())))
                    );
//@formatter:on
        }
    }

    @Inject
    EntityManagerHolder emh;

    private Class<T> entityClass;

    static class Data<T> {
        private List<T> items;

        public List<T> getItems() {
            return items;
        }

        public void setItems(List<T> objects) {
            this.items = objects;
        }
    }

    BindingGroup<Data<T>> data = new BindingGroup<>(new Data<>());

    Data<T> data() {
        return data.proxy();
    }

    private EntityManager getEm() {
        return emh.getEntityManager(emQualifier);
    }

    List<Attribute<?, ?>> properties;

    private List<CrudPropertyFilter> filterList;

    private Class<? extends Annotation> emQualifier;

    @Labeled
    @GlyphiconIcon(Glyphicon.check)
    public void pick(T item) {
        pickerClosed.fire(item);
    }

    @Labeled
    @GlyphiconIcon(Glyphicon.arrow_left)
    public void cancel() {
        pickerClosed.fire(null);
    }

    @Labeled
    @GlyphiconIcon(Glyphicon.search)
    public void search() {
        EntityManager em = getEm();

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> q = cb.createQuery(entityClass);
        Root<T> root = q.from(entityClass);
        q.select(root);

        ArrayList<Predicate> whereClauses = new ArrayList<>();
        for (CrudPropertyFilter filter : filterList) {
            filter.applyFilter(new CrudFilterPersitenceContext() {

                @Override
                public CriteriaQuery<?> query() {
                    return q;
                }

                @Override
                public CriteriaBuilder cb() {
                    return cb;
                }

                @Override
                public Root<?> root() {
                    return root;
                }

                @Override
                public void addWhere(Predicate predicate) {
                    whereClauses.add(predicate);
                }
            });
        }

        q.where(whereClauses.toArray(new Predicate[] {}));

        data.get().setItems(em.createQuery(q).getResultList());
        data.pullUp();
    }

    Mode mode = Mode.BROWSER;

    public DefaultCrudBrowserController<T> toPicker() {
        mode = Mode.PICKER;
        return this;
    }

    PersistentTypeIdentifier type;

    public DefaultCrudBrowserController<T> initialize(Class<T> entityClass,
            Class<? extends Annotation> emQualifier) {
        Preconditions.checkNotNull(entityClass, "entityClass is null");
        this.entityClass = entityClass;
        this.emQualifier = emQualifier;
        type = new PersistentTypeIdentifier(emQualifier, entityClass);

        properties = crudReflectionUtil.getBrowserProperties2(type);
        filterList = properties.stream().map(filters::getFactory)
                .collect(toList());

        search();

        return this;
    }

    GenericEvent<Object> pickerClosed = new GenericEvent<>();

    @Override
    public GenericEvent<Object> pickerClosed() {
        return pickerClosed;
    }

}