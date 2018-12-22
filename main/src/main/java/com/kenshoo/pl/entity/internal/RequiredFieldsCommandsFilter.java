package com.kenshoo.pl.entity.internal;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.kenshoo.pl.entity.*;

import java.util.Collection;
import java.util.Set;

public class RequiredFieldsCommandsFilter<E extends EntityType<E>> implements CommandsFilter<E> {

    private final Set<EntityField<E, ?>> requiredFields;

    public RequiredFieldsCommandsFilter(final Set<EntityField<E, ?>> requiredFields) {
        this.requiredFields = requiredFields;
    }

    @Override
    public <C extends ChangeEntityCommand<E>> Collection<C> filter(Collection<C> commands, final ChangeContext changeContext) {
        return Collections2.filter(commands, command -> Iterables.all(requiredFields, entityField -> {
            boolean fieldSpecified = command.isFieldChanged(entityField) && command.get(entityField) != null;
            if (!fieldSpecified) {
                changeContext.addValidationError(command,
                        new ValidationError(Errors.FIELD_IS_REQUIRED, entityField, ImmutableMap.of("field", entityField.toString())));
            }
            return fieldSpecified;
        }));
    }
}
