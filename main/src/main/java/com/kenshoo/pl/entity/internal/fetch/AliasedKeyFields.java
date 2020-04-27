package com.kenshoo.pl.entity.internal.fetch;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.TableField;

import java.util.List;
import java.util.stream.Collectors;

public class AliasedKeyFields {

    private static final String PREFIX = "key_field_";

    public static List<Field<?>> aliasedFields(List<TableField<Record, ?>> fields) {
        return (fields).stream()
                .map(field -> field.as(PREFIX + field.getName()))
                .collect(Collectors.toList());
    }
    public static <E extends EntityType<E>> String aliasOf(EntityField<E, ?> field) {
        return PREFIX + field.getDbAdapter().getFirstTableField().getName();
    }
}
