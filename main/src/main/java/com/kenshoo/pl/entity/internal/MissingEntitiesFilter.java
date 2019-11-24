package com.kenshoo.pl.entity.internal;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.kenshoo.pl.entity.ChangeContext;
import com.kenshoo.pl.entity.ChangeEntityCommand;
import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.Entity;
import com.kenshoo.pl.entity.EntityChange;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.SupportedChangeOperation;
import com.kenshoo.pl.entity.ValidationError;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

public class MissingEntitiesFilter<E extends EntityType<E>> implements ChangesFilter<E> {

    private final E entityType;

    public MissingEntitiesFilter(E entityType) {
        this.entityType = entityType;
    }

    @Override
    public <T extends EntityChange<E>> Collection<T> filter(Collection<T> changes, ChangeOperation changeOperation, ChangeContext changeContext) {
        return Collections2.filter(changes, command -> {
                    Entity entity = changeContext.getEntity(command);
                    if (entity == Entity.EMPTY) {
                        if(!command.allowMissingEntity()) {
                            changeContext.addValidationError(command, new ValidationError(Errors.ENTITY_NOT_FOUND, ImmutableMap.of("id", command.getIdentifier().toString())));
                        }
                        return false;
                    }
                    return true;
                }
        );
    }

    @Override
    public Stream<EntityField<?, ?>> getRequiredFields(Collection<? extends ChangeEntityCommand<E>> changeEntityCommands, ChangeOperation changeOperation) {
            return Stream.empty();
    }

    @Override
    public Stream<? extends EntityField<?, ?>> requiredFields(Collection<? extends EntityField<E, ?>> fieldsToUpdate, ChangeOperation changeOperation) {
        return Stream.empty();
    }

    @Override
    public SupportedChangeOperation getSupportedChangeOperation() {
        return SupportedChangeOperation.UPDATE_AND_DELETE;
    }
}
