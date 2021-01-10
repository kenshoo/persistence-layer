package com.kenshoo.pl.entity.internal;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.converters.IdentityValueConverter;
import org.jooq.Record;
import org.jooq.TableField;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.jooq.lambda.Seq.seq;

public class SecondaryTableUtil {

    public static <E extends EntityType<E>> Map<DataTable, EntityField<E, ?>> foreignKeysOfSecondaryTables(Collection<DataTable> secondaryTables, E entityType) {
        return seq(secondaryTables)
                .collect(Collectors.toMap(table -> table, table -> {
                    var foreignKey = table.getForeignKey(entityType.getPrimaryTable());
                    var primaryField = foreignKey.getKey().getFields().get(0);
                    var secondaryField = foreignKey.getFields().get(0);
                    return entityType.findField(secondaryField).orElseGet(() -> createTemporaryEntityField(entityType, secondaryField, primaryField));
                }));
    }

    private static <T, E extends EntityType<E>> EntityField<E, ?> createTemporaryEntityField(E entityType, TableField<Record, T> secondaryField, TableField<?, ?> primaryTableField) {
        var primaryField = entityType.findField(primaryTableField).get();
        var converter = IdentityValueConverter.getInstance(secondaryField.getType());
        return new EntityFieldImpl(entityType, new SimpleEntityFieldDbAdapter<>(secondaryField, converter), primaryField.getStringValueConverter(), Objects::equals);
    }
}
