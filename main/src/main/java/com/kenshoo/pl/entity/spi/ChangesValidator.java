package com.kenshoo.pl.entity.spi;

import com.kenshoo.pl.entity.ChangeContext;
import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.EntityChange;
import com.kenshoo.pl.entity.EntityType;

import java.util.Collection;

/**
 * The most generic validator interface. Should be implemented directly only if a complex, bulk
 * validation is required, like checking for already existing duplicates in DB. For validating
 * specific fields prefer implementing other interfaces in this package like {@link FieldValidator},
 * {@link FieldsCombinationValidator} or {@link FieldComplexValidator}.
 */
public interface ChangesValidator<E extends EntityType<E>> extends CurrentStateConsumer<E> {

    void validate(Collection<? extends EntityChange<E>> entityChanges, ChangeOperation changeOperation, ChangeContext changeContext);
}
