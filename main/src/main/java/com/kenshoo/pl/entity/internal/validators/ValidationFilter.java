package com.kenshoo.pl.entity.internal.validators;

import com.google.common.collect.Collections2;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.ChangesFilter;
import com.kenshoo.pl.entity.spi.ChangesValidator;
import com.kenshoo.pl.entity.spi.CurrentStateConsumer;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class ValidationFilter<E extends EntityType<E>> implements ChangesFilter<E> {

    private final List<ChangesValidator<E>> validators;

    public ValidationFilter(List<ChangesValidator<E>> validators) {
        this.validators = validators;
    }

    public <T extends EntityChange<E>> Collection<T> filter(Collection<T> commands, final ChangeOperation changeOperation, final ChangeContext changeContext) {
        validators.stream().filter(CurrentStateConsumer.supporting(changeOperation)).
                forEach(validator -> {
                    final Collection<T> collection = changeContext.isEnabled(Feature.RequiredFieldValidator) ?
                            Collections2.filter(commands, entityChange -> !changeContext.containsShowStopperErrorNonRecursive(entityChange))
                            : commands;
                    validator.validate(collection, changeOperation, changeContext);
                });
        return Collections2.filter(commands, entityChange -> !changeContext.containsErrorNonRecursive(entityChange));
    }

    @Override
    public Stream<? extends EntityField<?, ?>> requiredFields(Collection<? extends EntityField<E, ?>> fieldsToUpdate, ChangeOperation changeOperation) {
        return validators.stream()
                .filter(CurrentStateConsumer.supporting(changeOperation))
                .flatMap(changesValidator -> changesValidator.requiredFields(fieldsToUpdate, changeOperation));
    }

    @Override
    public SupportedChangeOperation getSupportedChangeOperation() {
        return SupportedChangeOperation.CREATE_UPDATE_AND_DELETE;
    }
}
