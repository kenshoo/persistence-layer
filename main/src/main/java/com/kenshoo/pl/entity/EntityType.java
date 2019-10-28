package com.kenshoo.pl.entity;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.data.CreateRecordCommand;
import com.kenshoo.pl.entity.annotation.IdGeneration;
import org.jooq.Key;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.lambda.tuple.Tuple2;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
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

        public final Collection<Tuple2<EntityField<FROM, ?>, EntityField<TO, ?>>> references;

        public ForeignKey(Collection<EntityField<FROM, ?>> from, Collection<EntityField<TO, ?>> to) {
            this.references = seq(from).zip(to).toList();
        }

        public ForeignKey(Iterable<Tuple2<EntityField<FROM, ?>, EntityField<TO, ?>>> references) {
            this.references = seq(references).toList();
        }

        public boolean notEmpty() {
            return size() > 0;
        }

        public int size() {
            return references.size();
        }

        public Collection<? extends EntityField<FROM, ?>> from() {
            return seq(references).map(pair -> pair.v1).toList();
        }

        public Collection<? extends EntityField<TO, ?>> to() {
            return seq(references).map(pair -> pair.v2).toList();
        }

        public ForeignKey<FROM, TO> filterByTo(Predicate<EntityField<TO, ?>> predicate) {
            return new ForeignKey<>(seq(references).filter(pair -> predicate.test(pair.v2)));
        }

        @Override
        public String toString() {
            return seq(references).map(pair -> "[" + fieldName(pair.v1) + "-->" + fieldName(pair.v2) + "]").toString(", ");
        }

        private String fieldName(EntityField field) {
            return field.getDbAdapter().getTable().getName() + "." + field;
        }
    }
}
