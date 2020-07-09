package com.kenshoo.pl.entity.internal;

import com.google.common.annotations.VisibleForTesting;
import com.kenshoo.pl.entity.ChangeContext;
import com.kenshoo.pl.entity.ChangeEntityCommand;
import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.CurrentEntityState;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.FieldChange;
import com.kenshoo.pl.entity.SupportedChangeOperation;
import com.kenshoo.pl.entity.spi.PostFetchCommandEnricher;
import org.jooq.lambda.Seq;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
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
            CurrentEntityState currentState = changeContext.getEntity(command);
            // Collect the fields first to avoid modification of command's inner collection while traversing
            List<FieldChange<E, ?>> unchangedFields = command.getChanges()
                    .filter(fieldChange -> areEqual(currentState, fieldChange))
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
    public Stream<EntityField<?, ?>> requiredFields(Collection<? extends EntityField<E, ?>> fieldsToUpdate, ChangeOperation changeOperation) {
        return Seq.seq(fieldsToUpdate);
    }

    @Override
    public SupportedChangeOperation getSupportedChangeOperation() {
        return SupportedChangeOperation.UPDATE;
    }

    private <T> boolean areEqual(CurrentEntityState currentState, FieldChange<E, T> fieldChange) {
        if (!currentState.containsField(fieldChange.getField())) {
            return false;
        }
        T v1 = fieldChange.getValue();
        T v2 =  currentState.get(fieldChange.getField());
        return fieldChange.getField().valuesEqual(v1, v2);
    }

    @VisibleForTesting
    public Set<EntityField<E, ?>> getFieldsToRetain() {
        return fieldsToRetain;
    }

    public static class Builder<E extends EntityType<E>> {
        private BiConsumer<ChangeEntityCommand<E>, EntityField<E, ?>> fieldUnsetter;
        private Set<EntityField<E, ?>> deleteIfSetAloneFields = emptySet();
        private Set<EntityField<E, ?>> fieldsToRetain = new HashSet<>();

        public Builder<E> setFieldUnsetter(BiConsumer<ChangeEntityCommand<E>, EntityField<E, ?>> fieldUnsetter) {
            this.fieldUnsetter = fieldUnsetter;
            return this;
        }

        public Builder<E> setDeleteIfSetAloneFields(Stream<EntityField<E, ?>> deleteIfSetAloneFields) {
            this.deleteIfSetAloneFields = deleteIfSetAloneFields.collect(toSet());
            return this;
        }

        public Builder<E> setDeleteIfSetAloneFields(EntityField<E, ?>... deleteIfSetAloneFields) {
            return setDeleteIfSetAloneFields(Stream.of(deleteIfSetAloneFields));
        }

        public Builder<E> addFieldsToRetain(Stream<EntityField<E, ?>> fieldsToRetain) {
            fieldsToRetain.forEach(this.fieldsToRetain::add);
            return this;
        }

        public Builder<E> addFieldsToRetain(EntityField<E, ?>... fieldsToRetain) {
            return addFieldsToRetain(Stream.of(fieldsToRetain));
        }

        public Builder<E> retainNonNullableFieldsOfSecondaryTables(E entityType) {
            return addFieldsToRetain(entityType.getFields()
                    .filter(belongingToSecondaryTable(entityType))
                    .filter(f -> isNotNullable(f)));
        }

        private <E extends EntityType<E>> boolean isNotNullable(EntityField<E, ?> field) {
            return !field.getDbAdapter().getTableFields().anyMatch(tableField -> tableField.getDataType().nullable());
        }

        private <E extends EntityType<E>> Predicate<EntityField<E, ?>> belongingToSecondaryTable(E entityType) {
            return f -> f.getDbAdapter().getTable() != entityType.getPrimaryTable();
        }

        public FalseUpdatesPurger<E> build() {
            return new FalseUpdatesPurger<>(fieldUnsetter, deleteIfSetAloneFields, fieldsToRetain);
        }

    }

}
