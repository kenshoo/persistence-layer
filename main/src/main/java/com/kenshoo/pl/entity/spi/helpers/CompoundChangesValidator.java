package com.kenshoo.pl.entity.spi.helpers;

import com.google.common.collect.Lists;
import com.kenshoo.pl.entity.ChangeContext;
import com.kenshoo.pl.entity.ChangeEntityCommand;
import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.EntityChange;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.spi.ChangesValidator;
import com.kenshoo.pl.entity.spi.CurrentStateConsumer;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class CompoundChangesValidator<E extends EntityType<E>> implements ChangesValidator<E> {

    private final List<ChangesValidator<E>> changesValidators = Lists.newArrayList();

    public void register(ChangesValidator<E> changesValidator) {
        changesValidators.add(changesValidator);
    }

    @Override
    public void validate(Collection<? extends EntityChange<E>> entityChanges, ChangeOperation changeOperation, ChangeContext changeContext) {
        changesValidators.stream().
                filter(CurrentStateConsumer.supporting(changeOperation)).
                    forEach(validator -> validator.validate(entityChanges, changeOperation, changeContext));
    }

    @Override
    public Stream<EntityField<?, ?>> getRequiredFields(Collection<? extends ChangeEntityCommand<E>> commands, ChangeOperation changeOperation) {
        return changesValidators.stream()
                .filter(CurrentStateConsumer.supporting(changeOperation))
                .flatMap(changesValidator -> changesValidator.getRequiredFields(commands, changeOperation));
    }

    @Override
    public Stream<? extends EntityField<?, ?>> requiredFields(Collection<? extends EntityField<E, ?>> fieldsToUpdate, ChangeOperation changeOperation) {
        return changesValidators.stream()
                .filter(CurrentStateConsumer.supporting(changeOperation))
                .flatMap(changesValidator -> changesValidator.requiredFields(fieldsToUpdate, changeOperation));
    }
}
