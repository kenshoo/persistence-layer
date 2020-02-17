package com.kenshoo.pl.entity.spi;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.SingleFieldEnricher;

abstract public class ExistingFieldModifier<E extends EntityType<E>, T> extends SingleFieldEnricher<E, T> {

    @Override
    final protected boolean shouldRunForCommand(EntityChange<E> entityChange) {
        return enrichedFieldExists(entityChange);
    }

    private boolean enrichedFieldExists(EntityChange<E> entityChange) {
        return entityChange.isFieldChanged(enrichedField());
    }
}
