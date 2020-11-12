package com.kenshoo.pl.entity.internal.fetch;

import com.kenshoo.jooq.TempTableResource;
import com.kenshoo.pl.data.ImpersonatorTable;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.IdentifierType;
import com.kenshoo.pl.entity.UniqueKey;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.lambda.Seq;

import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.lambda.Seq.seq;

public class AliasedKey <E extends EntityType<E>> {

    private static final String PREFIX = "key_field_";
    private final List<Field<E>> fields;

    public AliasedKey(IdentifierType<E> key) {
        this.fields = Seq.of(key.getFields())
                .map(field -> {
                    TableField<Record, ?> tableField = field.getDbAdapter().getFirstTableField();
                    return new Field<E>(field, tableField.as(PREFIX + tableField.getName()));
                }).collect(Collectors.toList());
    }

    // foreign key
    public AliasedKey(IdentifierType<E> key, TempTableResource<ImpersonatorTable> otherTable) {
        this.fields = Seq.of(key.getFields())
                .map(field -> {
                    TableField<Record, ?> tableField = otherTable.getTable().getField(field.getDbAdapter().getFirstTableField());
                    return new Field<E>(field, tableField.as(PREFIX + tableField.getName()));
                }).collect(Collectors.toList());
    }

    public List<Field<E>> fields() {
        return this.fields;
    }

    public List<? extends EntityField<E, ?>> unAliasedFields() {
        return seq(this.fields).map(Field::unAliased).toList();
    }

    public List<? extends org.jooq.Field<?>> aliasedFields() {
        return seq(this.fields).map(Field::aliased).toList();
    }

    public class Field<E extends EntityType<E>> {
        private final EntityField<E, ?> original;
        private final org.jooq.Field<?> aliased;

        public Field(EntityField<E, ?> original, org.jooq.Field<?> aliased) {
            this.original = original;
            this.aliased = aliased;
        }

        public org.jooq.Field<?> aliased(){
            return aliased;
        }

        public EntityField<E,?> unAliased(){
            return original;
        }
    }
}
