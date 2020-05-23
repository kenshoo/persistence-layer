package com.kenshoo.pl.entity.internal;

import com.google.common.collect.Collections2;
import com.kenshoo.pl.entity.ChangeContext;
import com.kenshoo.pl.entity.ChangeEntityCommand;
import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.Entity;
import com.kenshoo.pl.entity.EntityChange;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.SupportedChangeOperation;
import com.kenshoo.pl.entity.ValidationError;

import java.util.Collection;
import java.util.stream.Stream;

public class MissingParentEntitiesFilter<E extends EntityType<E>> implements ChangesFilter<E> {

    private final Collection<EntityField<E, ?>> foreignKeys;

    public MissingParentEntitiesFilter(Collection<EntityField<E, ?>> foreignKeys) {
        this.foreignKeys = foreignKeys;
    }

    @Override
    public <T extends ChangeEntityCommand<E>> Collection<T> filter(Collection<T> changes, ChangeOperation changeOperation, ChangeContext changeContext) {
        if (foreignKeys.isEmpty()) {
            return changes;
        }
        return Collections2.filter(changes, command -> {
                    Entity entity = changeContext.getEntity(command);
                    if (entity == Entity.EMPTY) {
                        changeContext.addValidationError(command, new ValidationError(Errors.PARENT_ENTITY_NOT_FOUND));
                        return false;
                    }
                    return true;
                }
        );
    }

    @Override
    public Stream<? extends EntityField<?, ?>> requiredFields(Collection<? extends EntityField<E, ?>> fieldsToUpdate, ChangeOperation changeOperation) {
        return Stream.of();
    }

    @Override
    public SupportedChangeOperation getSupportedChangeOperation() {
        return SupportedChangeOperation.CREATE;
    }
}
