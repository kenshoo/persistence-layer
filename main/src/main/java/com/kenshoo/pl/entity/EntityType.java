package com.kenshoo.pl.entity;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.data.CreateRecordCommand;
import com.kenshoo.pl.entity.annotation.IdGeneration;
import org.jooq.Key;
import org.jooq.Record;
import org.jooq.TableField;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.kenshoo.pl.data.CreateRecordCommand.OnDuplicateKey.FAIL;
import static java.util.function.Predicate.isEqual;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.jooq.lambda.Seq.seq;

public interface EntityType<E extends EntityType<E>> {

    String getName();

    DataTable getPrimaryTable();

    default Optional<EntityField<E, Object>> getPrimaryIdentityField() {
        return Optional.empty();
    }

    default Optional<EntityField<E, ? extends Number>> getIdField() {
        return Optional.empty();
    }

    default Optional<IdGeneration> getIdGeneration() {
        return Optional.empty();
    }


    Stream<EntityField<E, ?>> getFields();

    Stream<PrototypedEntityField<E, ?>> getPrototypedFields();

    EntityField<E, ?> getFieldByName(String name);

    String toFieldName(EntityField<E, ?> field);

    default SupportedChangeOperation getSupportedOperation() {
        return SupportedChangeOperation.CREATE_AND_UPDATE;
    }

    default CreateRecordCommand.OnDuplicateKey onDuplicateKey() {
        return FAIL;
    }

    default Collection<EntityField<E, ?>> findFields(Iterable<TableField<Record, ?>> tableFields) {
        return seq(tableFields)
                .map(tableField -> findField(tableField))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    default Optional<EntityField<E, ?>> findField(TableField tableField) {
        return getFields()
                .filter(entityField -> entityField.getDbAdapter().getTableFields().anyMatch(isEqual(tableField)))
                .findFirst();
    }

    default Optional<TableField<Record, ?>> findFirstTableField(EntityField<E, ?> entityField) {
        return entityField.getDbAdapter().getTableFields().findFirst();
    }

    default Collection<EntityField<E, ?>> determineForeignKeys(Set<EntityField<E, ?>> requiredFields) {
        Set<TableField<Record, ?>> foreignKeyFields = getPrimaryTable().getReferences().stream()
                .map(Key::getFields)
                .flatMap(Collection::stream)
                .collect(toSet());
        return requiredFields.stream().filter(entityField -> entityField.getDbAdapter().getTableFields().anyMatch(foreignKeyFields::contains)).collect(toList());
    }

    default <TO extends EntityType<TO>> ForeignKey<E, TO> getKeyTo(EntityType<TO> other) {
        org.jooq.ForeignKey<Record, Record> foreignKey = this.getPrimaryTable().getForeignKey(other.getPrimaryTable());
        return new ForeignKey<>(
                this.findFields(foreignKey.getFields()),
                other.findFields(foreignKey.getKey().getFields())
        );
    }
    class ForeignKey<FROM extends EntityType<FROM>, TO extends EntityType<TO>> {
        public ForeignKey(Collection<EntityField<FROM, ?>> from, Collection<EntityField<TO, ?>> to) {
            this.from = from;
            this.to = to;
        }
        public final Collection<EntityField<FROM, ?>> from;
        public final Collection<EntityField<TO, ?>> to;
    }
}
