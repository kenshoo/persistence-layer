package com.kenshoo.pl.entity.internal.fetch;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.TableField;

import java.util.List;
import java.util.stream.Collectors;

public class AliasedKeyFields<E extends EntityType<E>> {

    private final String PREFIX = "key_field_";

    public List<? extends Field<?>> aliasedFields(List<TableField<Record, ?>> fields) {
        return (fields).stream()
                .map(field -> field.as(PREFIX + field.getName()))
                .collect(Collectors.toList());
    }
    public String aliasOf(EntityField<E, ?> field) {
        return PREFIX + field.getDbAdapter().getFirstTableField().getName();
    }
}
