package com.kenshoo.pl.entity.internal;

import com.google.common.collect.Maps;
import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.CurrentEntityState;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.converters.IdentityValueConverter;
import org.jooq.Record;
import org.jooq.TableField;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

import static java.util.stream.Collectors.toList;
import static org.jooq.lambda.Seq.seq;

public class SecondaryTableAlreadyExistChecker<E extends EntityType<E>> {


    private final E entityType;
    private final ConcurrentMap<DataTable, EntityField<E, ?>> cache = Maps.newConcurrentMap();

    public SecondaryTableAlreadyExistChecker(E entityType) {
        this.entityType = entityType;
    }

    public List<EntityField<E, ?>> fieldsToFetch(Collection<DataTable> secondaryTables) {
        return seq(secondaryTables).map(table -> cache.computeIfAbsent(table, __ -> {
            var foreignKey = table.getForeignKey(entityType.getPrimaryTable());
            var primaryField = foreignKey.getKey().getFields().get(0);
            var secondaryField = foreignKey.getFields().get(0);
            return entityType.findField(secondaryField).orElseGet(() -> createTemporaryEntityField(secondaryField, primaryField));
        })).collect(toList());
    }

    private <T> EntityField<E, ?> createTemporaryEntityField(TableField<Record, T> secondaryField, TableField<?, ?> primaryTableField) {
        var primaryField = entityType.findField(primaryTableField).get();
        var converter = IdentityValueConverter.getInstance(secondaryField.getType());
        return new EntityFieldImpl(entityType, new SimpleEntityFieldDbAdapter<>(secondaryField, converter), primaryField.getStringValueConverter(), Objects::equals);
    }

    public boolean check(DataTable table, CurrentEntityState fetchedFields) {
        return !fetchedFields.safeGet(cache.get(table)).isNullOrAbsent();
    }
}
