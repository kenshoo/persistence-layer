package com.kenshoo.pl.entity.internal.validators;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.kenshoo.pl.entity.ChangeContext;
import com.kenshoo.pl.entity.ChangeEntityCommand;
import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.EntityChange;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.SupportedChangeOperation;
import com.kenshoo.pl.entity.internal.ChangesFilter;
import com.kenshoo.pl.entity.spi.ChangesValidator;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class ValidationFilter<E extends EntityType<E>> implements ChangesFilter<E> {

    private final List<ChangesValidator<E>> validators;

    public ValidationFilter(List<ChangesValidator<E>> validators) {
        this.validators = validators;
    }

    public <T extends EntityChange<E>> Collection<T> filter(Collection<T> commands, final ChangeOperation changeOperation, final ChangeContext changeContext) {
        for (ChangesValidator<E> validator : validators) {
            validator.validate(commands, changeOperation, changeContext);
        }
        return Collections2.filter(commands, (Predicate<EntityChange<E>>) entityChange -> !changeContext.containsErrorNonRecursive(entityChange));
    }

    @Override
    public Stream<EntityField<?, ?>> getRequiredFields(Collection<? extends ChangeEntityCommand<E>> commands, ChangeOperation changeOperation) {
        return validators.stream()
                .flatMap(validator -> validator.getRequiredFields(commands, changeOperation));
    }

    @Override
    public SupportedChangeOperation getSupportedChangeOperation() {
        return SupportedChangeOperation.CREATE_UPDATE_AND_DELETE;
    }
}
