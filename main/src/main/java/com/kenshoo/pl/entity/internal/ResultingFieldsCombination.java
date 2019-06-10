package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.Entity;
import com.kenshoo.pl.entity.EntityChange;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.FieldsValueMap;

import java.util.Collection;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

public class ResultingFieldsCombination<E extends EntityType<E>> implements FieldsValueMap<E> {
    private final EntityChange<E> entityChange;
    private final Entity entity;
    private final Collection<EntityField<E, ?>> fields;
    private final ChangeOperation changeOperation;

    public ResultingFieldsCombination(EntityChange<E> entityChange, Entity entity, Stream<EntityField<E, ?>> fields, ChangeOperation changeOperation) {
        this.entityChange = entityChange;
        this.entity = entity;
        this.fields = fields.collect(toSet());
        this.changeOperation = changeOperation;
    }

    @Override
    public <T> boolean containsField(EntityField<E, T> field) {
        return entityChange.containsField(field) || entity.containsField(field);
    }

    @Override
    public <T> T get(EntityField<E, T> field) {
        if (!fields.contains(field)) {
            throw new IllegalArgumentException("Illegal field: " + field);
        }

        if (entityChange.isFieldChanged(field)) {
            return entityChange.get(field);
        } else {
            if (changeOperation == ChangeOperation.UPDATE) {
                return entity.get(field);
            } else {
                return null;
            }
        }
    }
}
