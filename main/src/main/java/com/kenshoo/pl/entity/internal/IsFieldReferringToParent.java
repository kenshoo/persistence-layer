package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.Hierarchy;

import java.util.Collection;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;


public class IsFieldReferringToParent<E extends EntityType<E>> implements Predicate<EntityField<E, ?>> {

    private final Collection<? extends EntityField<E, ?>> childFieldsReferringToParent;

    public IsFieldReferringToParent(Hierarchy hierarchy, EntityType<E> child) {
        childFieldsReferringToParent = hierarchy.getParent(child).map(parent -> child.getKeyTo(parent).from())
                .orElse(emptyList());
    }

    @Override
    public boolean test(EntityField<E, ?> field) {
        return childFieldsReferringToParent.contains(field);
    }

}
