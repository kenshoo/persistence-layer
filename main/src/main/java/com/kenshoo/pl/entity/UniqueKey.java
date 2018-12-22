package com.kenshoo.pl.entity;

import com.google.common.collect.ImmutableList;
import org.jooq.Record;
import org.jooq.TableField;

import java.util.Arrays;
import java.util.List;


/**
 * Specifies UniqueKey of entry.
 * Since the key is used in maps, it's highly recommended to reuse the value of this class.
 * <p>
 * The instances of this class are immutable.
 */
public class UniqueKey<E extends EntityType<E>> {

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

    public EntityField<E, ?>[] getFields() {
        return fields;
    }

    public List<TableField<Record, ?>> getTableFields() {
        //noinspection unchecked
        return ImmutableList.copyOf(tableFields);
    }

    public Identifier<E> createValue(FieldsValueMap<E> fieldsValueMap) {
        Object[] values = new Object[fields.length];
        for (int i = 0; i < fields.length; i++) {
            values[i] = fieldsValueMap.get(fields[i]);
        }
        return new UniqueKeyValue<>(this, values);
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
