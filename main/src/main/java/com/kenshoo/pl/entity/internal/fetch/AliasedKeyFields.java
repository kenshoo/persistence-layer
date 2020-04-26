package com.kenshoo.pl.entity.internal.fetch;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.UniqueKey;
import org.jooq.Field;
import org.jooq.lambda.Seq;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AliasedKeyFields<E extends EntityType<E>> {

    private final Map<EntityField<E, ?>, Field<?>> map;

    public AliasedKeyFields(UniqueKey<E> uniqueKey) {
        this.map = createAliasedField(Seq.of(uniqueKey.getFields()).toMap(field -> field, field -> field.getDbAdapter().getFirstTableField()));
    }

    public AliasedKeyFields(Map<EntityField<E, ?>, Field<?>> fieldDBFieldMap) {
        this.map = createAliasedField(fieldDBFieldMap);
    }

    public Set<EntityField<E, ?>> keyFields() {
        return map.keySet();
    }

    public Collection<Field<?>> aliasedFields() {
        return map.values();
    }

    public String aliasOf(EntityField<E, ?> field) {
        return map.get(field).getName();
    }

    private Map<EntityField<E, ?>, Field<?>> createAliasedField(Map<EntityField<E, ?>,  Field<?>> fieldDBFieldMap) {
        Map<EntityField<E, ?>, Field<?>> fieldAliasedFieldMap = new HashMap<>();
        int keyFieldIndex = 0;
        for (Map.Entry<EntityField<E, ?>, Field<?>> fieldDBField : fieldDBFieldMap.entrySet()) {
            fieldAliasedFieldMap.put(fieldDBField.getKey(), fieldDBField.getValue().as(withAlias(keyFieldIndex)));
            keyFieldIndex++;
        }
        return fieldAliasedFieldMap;
    }

    private String withAlias(int fieldIndex) {
        return "key_field_" + fieldIndex;
    }
}
