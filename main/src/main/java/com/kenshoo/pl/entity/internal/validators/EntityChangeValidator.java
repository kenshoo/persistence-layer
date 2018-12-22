package com.kenshoo.pl.entity.internal.validators;

import com.kenshoo.pl.entity.*;

import java.util.stream.Stream;

/**
 * Created by dimag on 06/12/2015.
 */
public interface EntityChangeValidator<E extends EntityType<E>> {

    Stream<EntityField<E, ?>> getValidatedFields();

    SupportedChangeOperation getSupportedChangeOperation();

    Stream<? extends EntityField<?, ?>> getFieldsToFetch(ChangeOperation changeOperation);

    ValidationError validate(EntityChange<E> entityChange, Entity entity, ChangeOperation changeOperation);

}
