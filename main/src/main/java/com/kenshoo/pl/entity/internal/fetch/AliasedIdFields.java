package com.kenshoo.pl.entity.internal.fetch;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.UniqueKey;
import org.jooq.Field;

import java.util.HashMap;
import java.util.Map;

public class AliasedIdFields<E extends EntityType<E>> {

    private final Map<EntityField<E, ?>, Field<?>> aliasedFields;

    public AliasedIdFields(UniqueKey<E> uniqueKey) {
        this.aliasedFields = createAliasedField(uniqueKey.getFields());
    }

    public Map<EntityField<E, ?>, Field<?>> getAliasedFields() {
        return aliasedFields;
    }

    private Map<EntityField<E, ?>, Field<?>> createAliasedField(EntityField<E, ?>[] ids) {
        Map<EntityField<E, ?>, Field<?>> fieldsWithAlias = new HashMap<>();
        int keyFieldIndex = 0;
        for (EntityField id : ids) {
            fieldsWithAlias.put(id, id.getDbAdapter().getFirstTableField().as(keyFieldAlias(keyFieldIndex)));
            keyFieldIndex++;
        }
        return fieldsWithAlias;
    }

    private String keyFieldAlias(int keyFieldIndex) {
        return "key_field_" + keyFieldIndex;
    }
}
