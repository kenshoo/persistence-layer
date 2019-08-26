package com.kenshoo.pl.entity.internal;

import com.google.common.annotations.VisibleForTesting;
import com.kenshoo.pl.entity.ChangeContext;
import com.kenshoo.pl.entity.ChangeEntityCommand;
import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.Entity;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.FieldChange;
import com.kenshoo.pl.entity.SupportedChangeOperation;
import com.kenshoo.pl.entity.spi.PostFetchCommandEnricher;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class FalseUpdatesPurger<E extends EntityType<E>> implements PostFetchCommandEnricher<E> {

    // Did not want to make ChangeEntityCommand.unset public so getting an accessor to call it
    private final BiConsumer<ChangeEntityCommand<E>, EntityField<E, ?>> fieldUnsetter;
    private final Set<EntityField<E, ?>> deleteIfSetAloneFields;

    private final Set<EntityField<E, ?>> fieldsToRetain;

    private FalseUpdatesPurger(
            BiConsumer<ChangeEntityCommand<E>, EntityField<E, ?>> fieldUnsetter,
            Set<EntityField<E, ?>> deleteIfSetAloneFields,
            Set<EntityField<E, ?>> fieldsToRetain) {

        this.fieldUnsetter = fieldUnsetter;
        this.deleteIfSetAloneFields = deleteIfSetAloneFields;
        this.fieldsToRetain = fieldsToRetain;
    }

    @Override
    public void enrich(Collection<? extends ChangeEntityCommand<E>> commands, ChangeOperation changeOperation, ChangeContext changeContext) {
        commands.forEach(command -> {
            Entity entity = changeContext.getEntity(command);
            // Collect the fields first to avoid modification of command's inner collection while traversing
            List<FieldChange<E, ?>> unchangedFields = command.getChanges()
                    .filter(fieldChange -> areEqual(entity, fieldChange))
                    .filter(change -> !fieldsToRetain.contains(change.getField()))
                    .collect(toList());
            unchangedFields.forEach(fieldChange -> fieldUnsetter.accept(command, fieldChange.getField()));
            //Contains only ignorable fields
            if(command.getChanges().allMatch(fieldChange -> deleteIfSetAloneFields.contains(fieldChange.getField()))) {
                deleteIfSetAloneFields.forEach(field -> fieldUnsetter.accept(command, field));
            }
        });
    }

    @Override
    public Stream<EntityField<?, ?>> getRequiredFields(Collection<? extends ChangeEntityCommand<E>> commands, ChangeOperation changeOperation) {
        return commands.stream().flatMap(ChangeEntityCommand::getChangedFields);
    }

    @Override
    public SupportedChangeOperation getSupportedChangeOperation() {
        return SupportedChangeOperation.UPDATE;
    }

    private <T> boolean areEqual(Entity entity, FieldChange<E, T> fieldChange) {
        if (!entity.containsField(fieldChange.getField())) {
            return false;
        }
        T v1 = fieldChange.getValue();
        T v2 = entity.get(fieldChange.getField());
        return fieldChange.getField().valuesEqual(v1, v2);
    }

    @VisibleForTesting
    public Set<EntityField<E, ?>> getFieldsToRetain() {
        return fieldsToRetain;
    }

    public static class Builder<E extends EntityType<E>> {
        private BiConsumer<ChangeEntityCommand<E>, EntityField<E, ?>> fieldUnsetter;
        private Set<EntityField<E, ?>> deleteIfSetAloneFields = emptySet();
        private Set<EntityField<E, ?>> fieldsToRetain = emptySet();

        public Builder setFieldUnsetter(BiConsumer<ChangeEntityCommand<E>, EntityField<E, ?>> fieldUnsetter) {
            this.fieldUnsetter = fieldUnsetter;
            return this;
        }

        public Builder setDeleteIfSetAloneFields(Stream<EntityField<E, ?>> deleteIfSetAloneFields) {
            this.deleteIfSetAloneFields = deleteIfSetAloneFields.collect(toSet());
            return this;
        }

        public Builder setDeleteIfSetAloneFields(EntityField<E, ?>... deleteIfSetAloneFields) {
            return setDeleteIfSetAloneFields(Stream.of(deleteIfSetAloneFields));
        }

        public Builder setFieldsToRetain(Stream<EntityField<E, ?>> fieldsToRetain) {
            this.fieldsToRetain = fieldsToRetain.collect(toSet());
            return this;
        }

        public Builder setFieldsToRetain(EntityField<E, ?>... fieldsToRetain) {
            return setFieldsToRetain(Stream.of(fieldsToRetain));
        }

        public FalseUpdatesPurger<E> build() {
            return new FalseUpdatesPurger<>(fieldUnsetter, deleteIfSetAloneFields, fieldsToRetain);
        }
    }

}
