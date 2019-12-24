package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.ChangeContext;
import com.kenshoo.pl.entity.ChangeEntityCommand;
import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.SupportedChangeOperation;
import com.kenshoo.pl.entity.spi.PostFetchCommandEnricher;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * Created by yuvalr on 2/1/16.
 */
public class DefaultFieldValueEnricher<E extends EntityType<E>, T> implements PostFetchCommandEnricher<E> {

    private final EntityField<E, T> field;
    private final T defaultValue;

    public DefaultFieldValueEnricher(EntityField<E, T> field, T defaultValue) {
        this.field = field;
        this.defaultValue = defaultValue;
    }

    @Override
    public void enrich(Collection<? extends ChangeEntityCommand<E>> changeEntityCommands, ChangeOperation changeOperation, ChangeContext changeContext) {
        changeEntityCommands.stream()
        .filter(command -> !command.isFieldChanged(field))
        .forEach(command -> command.set(field, defaultValue));
    }

    @Override
    public Stream<EntityField<E, ?>> fieldsToEnrich() {
        return Stream.of(field);
    }

    @Override
    public boolean shouldRun(Collection<? extends ChangeEntityCommand<E>> changeEntityCommands) {
        return changeEntityCommands.stream().anyMatch(cmd -> !cmd.isFieldChanged(field));
    }

    @Override
    public SupportedChangeOperation getSupportedChangeOperation() {
        return SupportedChangeOperation.CREATE;
    }

}
