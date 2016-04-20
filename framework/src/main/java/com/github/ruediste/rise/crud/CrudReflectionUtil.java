package com.github.ruediste.rise.crud;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;

import com.github.ruediste.c3java.properties.PropertyInfo;
import com.github.ruediste.c3java.properties.PropertyUtil;
import com.github.ruediste.rise.core.persistence.PersistentType;
import com.github.ruediste.rise.core.persistence.RisePersistenceUtil;
import com.github.ruediste.rise.crud.annotations.CrudBrowseAction;
import com.github.ruediste.rise.crud.annotations.CrudBrowserColumn;
import com.github.ruediste.rise.crud.annotations.CrudDisplayAction;
import com.github.ruediste.rise.crud.annotations.CrudIdentifying;
import com.github.ruediste.rise.crud.annotations.CrudValidationGroup;
import com.github.ruediste.rise.nonReloadable.front.reload.MemberOrderIndex;
import com.google.common.base.Preconditions;

@Singleton
public class CrudReflectionUtil {

    @Inject
    RisePersistenceUtil persistenceUtil;

    @Inject
    MemberOrderIndex memberOrderIndex;

    public List<CrudPropertyInfo> getDisplayProperties(PersistentType type) {
        return getAllProperties(type);
    }

    public List<CrudPropertyInfo> getEditProperties(PersistentType type) {
        return getAllProperties(type);
    }

    public Map<String, CrudPropertyInfo> getAllPropertiesMap(PersistentType type) {
        return getAllProperties(type).stream().collect(toMap(x -> x.getAttribute().getName(), x -> x));
    }

    public List<CrudPropertyInfo> getAllProperties(PersistentType type) {
        return getOrderedAttributes(type.getType()).stream()
                .map(toPersistentPropertyFunction(type.getEmQualifier())::apply).collect(toList());
    }

    public List<CrudPropertyInfo> getBrowserProperties(PersistentType type) {
        return getPropertiesAnnotatedWith(type, CrudBrowserColumn.class, CrudIdentifying.class);
    }

    public List<CrudPropertyInfo> getIdentificationProperties(PersistentType type) {
        return getPropertiesAnnotatedWith(type, CrudIdentifying.class);
    }

    @SafeVarargs
    final private List<CrudPropertyInfo> getPropertiesAnnotatedWith(PersistentType type,
            Class<? extends Annotation>... annotationClasses) {
        Preconditions.checkNotNull(type, "cls is null");
        List<Attribute<?, ?>> result = new ArrayList<>();

        ManagedType<?> type2 = type.getType();
        List<Attribute<?, ?>> orderedAttributes = getOrderedAttributes(type2);
        attributeLoop: for (Attribute<?, ?> attribute : orderedAttributes) {
            Member member = attribute.getJavaMember();
            if (member instanceof AnnotatedElement)
                for (Class<? extends Annotation> annotationClass : annotationClasses)
                    if (((AnnotatedElement) member).isAnnotationPresent(annotationClass)) {
                        result.add(attribute);
                        continue attributeLoop;
                    }
        }
        if (result.isEmpty())
            result = orderedAttributes;
        return result.stream().map(toPersistentPropertyFunction(type.getEmQualifier())::apply).collect(toList());
    }

    private List<Attribute<?, ?>> getOrderedAttributes(ManagedType<?> type2) {
        Preconditions.checkNotNull(type2, "type2");
        Map<Member, Attribute<?, ?>> memberMap = new HashMap<>();
        for (Attribute<?, ?> attr : type2.getAttributes()) {
            memberMap.put(attr.getJavaMember(), attr);
        }
        ArrayList<Attribute<?, ?>> result = new ArrayList<>();
        for (Member member : memberOrderIndex.orderMembers(type2.getJavaType(), memberMap.keySet())) {
            result.add(memberMap.get(member));
        }
        return result;
    }

    public Function<Attribute<?, ?>, CrudPropertyInfo> toPersistentPropertyFunction(
            Class<? extends Annotation> emQualifier) {
        return a -> toPersistentProperty(a, emQualifier);
    }

    public CrudPropertyInfo toPersistentProperty(Attribute<?, ?> attribute, Class<? extends Annotation> emQualifier) {
        PropertyInfo property = PropertyUtil.getPropertyInfo(attribute.getDeclaringType().getJavaType(),
                attribute.getName());

        return new CrudPropertyInfo(property, attribute, emQualifier);
    }

    public PersistentType getPersistentType(Object entity) {
        return getPersistentType(persistenceUtil.getEmQualifier(entity), entity.getClass());
    }

    public PersistentType getPersistentType(Class<? extends Annotation> emQualifier, Class<?> entityClass) {
        ManagedType<?> managedType = persistenceUtil.getManagedType(emQualifier, entityClass);
        if (managedType == null)
            throw new RuntimeException(
                    "No managed type found for " + entityClass + " using emQualifier " + emQualifier);
        return new PersistentType(emQualifier, entityClass, managedType);
    }

    public List<Method> getBrowseActionMethods(Class<?> cls) {
        return getActionMethods(cls, CrudBrowseAction.class);
    }

    public List<Method> getDisplayActionMethods(Class<?> cls) {
        return getActionMethods(cls, CrudDisplayAction.class);
    }

    private List<Method> getActionMethods(Class<?> cls, Class<? extends Annotation> annotationClass) {
        List<Method> result = Arrays.stream(cls.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(annotationClass)).collect(Collectors.toList());
        memberOrderIndex.orderMembers(cls, result);
        return result;
    }

    /**
     * determine the validation groups to be used for a certain type. Return
     * null if no validation should be performed
     */
    public Class<?>[] validationGroups(Class<?> type) {
        CrudValidationGroup group = type.getAnnotation(CrudValidationGroup.class);

        if (group == null || group.value().length == 0) {
            // no annotation or nothing set
            return new Class<?>[] {};
        } else if (group.value().length == 1 && group.value()[0].equals(Void.class)) {
            // group set to void, no validation
            return null;
        } else {
            // There is an annotation and some groups have been set. Use them.
            return group.value();
        }
    }
}
