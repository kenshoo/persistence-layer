package com.kenshoo.pl.entity.spi.helpers;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.ResultingFieldsCombination;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.stream.Collectors;

@Component
public class FieldsCombinationCreator<E extends EntityType<E>> {

    public Collection<? extends FieldsValueMap<E>> create(final Collection<? extends EntityChange<E>> entityChanges, final Collection<EntityField<E, ?>> fields, ChangeOperation changeOperation, ChangeContext changeContext) {
        return entityChanges.stream()
                .map(e -> new ResultingFieldsCombination<>(e, changeContext.getEntity(e), fields.stream(), changeOperation))
                .collect(Collectors.toList());
    }
}