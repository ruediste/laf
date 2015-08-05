package com.github.ruediste.rise.crud;

import static java.util.stream.Collectors.toList;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;

import com.github.ruediste.c3java.properties.PropertyInfo;
import com.github.ruediste.c3java.properties.PropertyUtil;
import com.github.ruediste.rise.core.persistence.PersistentType;
import com.github.ruediste.rise.core.persistence.RisePersistenceUtil;
import com.github.ruediste.rise.crud.annotations.CrudBrowserColumn;
import com.github.ruediste.rise.nonReloadable.front.reload.MemberOrderIndex;
import com.google.common.base.Preconditions;

@Singleton
public class CrudReflectionUtil {

    @Inject
    RisePersistenceUtil persistenceUtil;

    @Inject
    MemberOrderIndex memberOrderIndex;

    public PropertyInfo getProperty(Attribute<?, ?> attribute) {
        return PropertyUtil.getPropertyInfo(attribute.getDeclaringType()
                .getJavaType(), attribute.getName());
    }

    public List<PersistentProperty> getDisplayProperties(PersistentType type) {
        return getAllProperties(type);
    }

    public List<PersistentProperty> getEditProperties(PersistentType type) {
        return getAllProperties(type);
    }

    private List<PersistentProperty> getAllProperties(PersistentType type) {
        return getOrderedAttributes(type.getType()).stream()
                .map(this::toPersistentAttribute).collect(toList());
    }

    public List<PersistentProperty> getBrowserProperties(PersistentType type) {
        return getPropertiesAnnotatedWith(type, CrudBrowserColumn.class);
    }

    public List<PersistentProperty> getIdentificationProperties(
            PersistentType type) {
        return getPropertiesAnnotatedWith(type, CrudBrowserColumn.class);
    }

    private List<PersistentProperty> getPropertiesAnnotatedWith(
            PersistentType type, Class<? extends Annotation> annotationClass) {
        Preconditions.checkNotNull(type, "cls is null");
        List<Attribute<?, ?>> result = new ArrayList<>();

        ManagedType<?> type2 = type.getType();
        List<Attribute<?, ?>> orderedAttributes = getOrderedAttributes(type2);
        for (Attribute<?, ?> attribute : orderedAttributes) {
            Member member = attribute.getJavaMember();
            if (member instanceof AnnotatedElement)
                if (((AnnotatedElement) member)
                        .isAnnotationPresent(annotationClass))
                    result.add(attribute);
        }
        if (result.isEmpty())
            result = orderedAttributes;
        return result.stream().map(this::toPersistentAttribute)
                .collect(toList());
    }

    private List<Attribute<?, ?>> getOrderedAttributes(ManagedType<?> type2) {
        Map<Member, Attribute<?, ?>> memberMap = new HashMap<>();
        for (Attribute<?, ?> attr : type2.getAttributes()) {
            memberMap.put(attr.getJavaMember(), attr);
        }
        ArrayList<Attribute<?, ?>> result = new ArrayList<>();
        for (Member member : memberOrderIndex.orderMembers(type2.getJavaType(),
                memberMap.keySet())) {
            result.add(memberMap.get(member));
        }
        return result;
    }

    public PersistentProperty toPersistentAttribute(Attribute<?, ?> attribute) {
        PropertyInfo property = PropertyUtil.getPropertyInfo(attribute
                .getDeclaringType().getJavaType(), attribute.getName());

        return new PersistentProperty(property, attribute);
    }

    public PersistentType getPersistentType(Object entity) {
        return getPersistentType(persistenceUtil.getEmQualifier(entity),
                entity.getClass());
    }

    public PersistentType getPersistentType(
            Class<? extends Annotation> emQualifier, Class<?> entityClass) {
        return new PersistentType(emQualifier, entityClass,
                persistenceUtil.getManagedType(emQualifier, entityClass));
    }
}
