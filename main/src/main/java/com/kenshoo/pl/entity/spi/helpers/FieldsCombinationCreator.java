package com.kenshoo.pl.entity.spi.helpers;

import com.kenshoo.pl.entity.ChangeContext;
import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.EntityChange;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.FieldsValueMap;
import com.kenshoo.pl.entity.internal.FieldsCombination;

import java.util.Collection;
import java.util.stream.Collectors;

public class FieldsCombinationCreator {

    public static <E extends EntityType<E>> Collection<? extends FieldsValueMap<E>> create(final Collection<? extends EntityChange<E>> entityChanges, final Collection<EntityField<E, ?>> fields, ChangeOperation changeOperation, ChangeContext changeContext) {
        return entityChanges.stream()
                .map(e -> new FieldsCombination<>(e, changeContext.getEntity(e), fields.stream(), changeOperation))
                .collect(Collectors.toList());
    }
}