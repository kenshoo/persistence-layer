package com.kenshoo.pl.entity.spi.helpers;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.spi.PostFetchCommandEnricher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class CopyFieldsOnCreateEnricher<E extends EntityType<E>> implements PostFetchCommandEnricher<E> {

    final List<Field2Copy<E, ?>> fields2Copy = new ArrayList<>();
    protected final Set<EntityField<?, ?>> requiredFields = new HashSet<>();

    public <T> CopyFieldsOnCreateEnricher(EntityField<?, T> sourceField, EntityField<E, T> targetField) {
        //noinspection unchecked
        this(new Field2Copy<>(sourceField, targetField));
    }

    public <T1, T2> CopyFieldsOnCreateEnricher(EntityField<?, T1> sourceField1, EntityField<E, T1> targetField1,
                                               EntityField<?, T2> sourceField2, EntityField<E, T2> targetField2) {
        //noinspection unchecked
        this(new Field2Copy<>(sourceField1, targetField1), new Field2Copy<>(sourceField2, targetField2));
    }

    public <T1, T2, T3> CopyFieldsOnCreateEnricher(EntityField<?, T1> sourceField1, EntityField<E, T1> targetField1,
                                                   EntityField<?, T2> sourceField2, EntityField<E, T2> targetField2,
                                                   EntityField<?, T3> sourceField3, EntityField<E, T3> targetField3) {
        //noinspection unchecked
        this(new Field2Copy<>(sourceField1, targetField1),
                new Field2Copy<>(sourceField2, targetField2),
                new Field2Copy<>(sourceField3, targetField3));
    }

    public <T1, T2, T3, T4> CopyFieldsOnCreateEnricher(EntityField<?, T1> sourceField1, EntityField<E, T1> targetField1,
                                                   EntityField<?, T2> sourceField2, EntityField<E, T2> targetField2,
                                                   EntityField<?, T3> sourceField3, EntityField<E, T3> targetField3,
                                                   EntityField<?, T4> sourceField4, EntityField<E, T4> targetField4) {
        //noinspection unchecked
        this(new Field2Copy<>(sourceField1, targetField1),
                new Field2Copy<>(sourceField2, targetField2),
                new Field2Copy<>(sourceField3, targetField3),
                new Field2Copy<>(sourceField4, targetField4));
    }

    @SuppressWarnings("unchecked")
    private CopyFieldsOnCreateEnricher(Field2Copy<E, ?>... fieldsToCopy) {
        this.fields2Copy.addAll(Arrays.asList(fieldsToCopy));
        addRequiredFields();
    }

    protected void addRequiredFields() {
        this.fields2Copy.stream()
                        .map(input -> input.source)
                        .forEach(requiredFields::add);
    }

    @Override
    public SupportedChangeOperation getSupportedChangeOperation() {
        return SupportedChangeOperation.CREATE;
    }

    @Override
    public void enrich(Collection<? extends ChangeEntityCommand<E>> commands, ChangeOperation changeOperation, ChangeContext changeContext) {
        for (ChangeEntityCommand<E> command : commands) {
            Entity entity = changeContext.getEntity(command);
            for (Field2Copy<E, ?> field2Copy : fields2Copy) {
                copyField(field2Copy, entity, command);
            }
        }
    }

    @Override
    public Stream<EntityField<E, ?>> fieldsToEnrich() {
        return fields2Copy.stream().map(pair -> pair.target);
    }

    @Override
    public boolean shouldRun(Collection<? extends ChangeEntityCommand<E>> changeEntityCommands) {
        return true;
    }

    private <T> void copyField(Field2Copy<E, T> field2Copy, Entity entity, ChangeEntityCommand<E> command) {
        command.set(field2Copy.target, entity.get(field2Copy.source));
    }

    @Override
    public Stream<EntityField<?, ?>> getRequiredFields(Collection<? extends ChangeEntityCommand<E>> changeEntityCommands, ChangeOperation changeOperation) {
        return requiredFields.stream();
    }

    @Override
    public Stream<? extends EntityField<?, ?>> requiredFields(Collection<? extends EntityField<E, ?>> fieldsToUpdate, ChangeOperation changeOperation) {
        return requiredFields.stream();
    }


    public static class Field2Copy<E extends EntityType<E>, T> {
        private final EntityField<?, T> source;
        private final EntityField<E, T> target;

        Field2Copy(EntityField<?, T> source, EntityField<E, T> target) {
            this.source = source;
            this.target = target;
        }

        public EntityField<E, T> getTarget() {
            return target;
        }
    }

}
