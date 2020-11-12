package com.kenshoo.pl.entity;

import com.google.common.collect.ImmutableList;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.lambda.Seq;

import java.util.Arrays;
import java.util.List;

import static org.jooq.lambda.Seq.seq;


/**
 * Specifies UniqueKey of entry.
 * Since the key is used in maps, it's highly recommended to reuse the value of this class.
 * <p>
 * The instances of this class are immutable.
 */
public class UniqueKey<E extends EntityType<E>> implements IdentifierType<E> {

    private final EntityField<E, ?>[] fields;
    private final TableField<Record, ?>[] tableFields;
    private int hashCode;

    public UniqueKey(EntityField<E, ?>[] fields) {
        this.fields = fields;
        //noinspection unchecked
        this.tableFields = Arrays.asList(fields).stream()
                .flatMap(field -> field.getDbAdapter().getTableFields())
                .toArray(TableField[]::new);
    }

    public UniqueKey(Iterable<? extends EntityField<E, ?>> fields) {
        this(seq(fields).toArray(EntityField[]::new));
    }

    @Override
    public EntityField<E, ?>[] getFields() {
        return fields;
    }

    @Override
    public List<TableField<Record, ?>> getTableFields() {
        //noinspection unchecked
        return ImmutableList.copyOf(tableFields);
    }

    @Override
    public Identifier<E> createIdentifier(FieldsValueMap<E> fieldsValueMap) {
        Object[] values = new Object[fields.length];
        for (int i = 0; i < fields.length; i++) {
            values[i] = fieldsValueMap.get(fields[i]);
        }
        return new UniqueKeyValue<>(this, values);
    }

    @Override
    public Identifier<E> createIdentifier(Entity entity) {
        final Object[] values = Seq.of(fields)
                .map(entity::get)
                .toArray();
        return new UniqueKeyValue<>(this, values);
    }

    @Override
    public E getEntityType() {
        if(this.fields.length == 0) {
            throw new IllegalStateException("unique key does not contain any fields.");
        }
        return (E) this.fields[0].getEntityType();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UniqueKey)) return false;

        UniqueKey uniqueKey = (UniqueKey) o;

        return Arrays.deepEquals(fields, uniqueKey.fields);
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = Arrays.deepHashCode(fields);
        }
        return hashCode;
    }

    @Override
    public String toString() {
        return "UniqueKey{" +
                "fields=" + Arrays.toString(fields) +
                '}';
    }
}
