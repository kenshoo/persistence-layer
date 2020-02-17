package com.kenshoo.pl.entity.spi;

import com.kenshoo.pl.entity.EntityChange;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.internal.SingleFieldEnricher;

public abstract class MissingFieldEnricher<E extends EntityType<E>, T> extends SingleFieldEnricher<E, T> {

    @Override
    final protected boolean shouldRunForCommand(EntityChange<E> entityChange) {
        return shouldEnrichField(entityChange);
    }

    protected boolean considerNullAsMissing() {
        return false;
    }


    private boolean shouldEnrichField(EntityChange<E> entityChange) {
        return enrichedFieldIsMissing(entityChange) || (considerNullAsMissing() && enrichedFieldHasNullValue(entityChange));
    }

    private boolean enrichedFieldHasNullValue(EntityChange<E> entityChange) {
        return entityChange.get(enrichedField()) == null;
    }

    private boolean enrichedFieldIsMissing(EntityChange<E> entityChange) {
        return !entityChange.isFieldChanged(enrichedField());
    }
}
