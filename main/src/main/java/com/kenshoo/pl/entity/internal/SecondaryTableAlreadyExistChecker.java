package com.kenshoo.pl.entity.internal;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.CurrentEntityState;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.ValueConverter;
import com.kenshoo.pl.entity.converters.IdentityValueConverter;
import org.jooq.ForeignKey;
import org.jooq.Record;
import org.jooq.TableField;

import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.jooq.lambda.Seq.seq;

public class SecondaryTableAlreadyExistChecker<E extends EntityType<E>> {


    private final E entityType;
    private final Map<DataTable, EntityField<E, ?>> cache = new HashMap<>();

    public SecondaryTableAlreadyExistChecker(E entityType) {
        this.entityType = entityType;
    }

    public List<EntityField<E, ?>> fieldsToFetch(Collection<DataTable> secondaryTables) {
        return seq(secondaryTables).map(table-> cache.computeIfAbsent(table, __ -> {
            final ForeignKey<Record, Record> foreignKey = table.getForeignKey(entityType.getPrimaryTable());
            final TableField<?, ?> primaryField = foreignKey.getKey().getFields().get(0);
            final TableField<Record, ?> secondaryField = foreignKey.getFields().get(0);
            return entityType.findField(secondaryField).orElse(createEntityField(secondaryField, primaryField));
        })).collect(toList());
    }

    private <T> EntityField<E, ?> createEntityField(TableField<Record, T> secondaryField, TableField<?, ?> primaryTableField) {
        final EntityField<E, ?> primaryField = entityType.findField(primaryTableField).get();
        final ValueConverter<T, T> converter = IdentityValueConverter.getInstance(secondaryField.getType());
        return new EntityFieldImpl(entityType, new SimpleEntityFieldDbAdapter<>(secondaryField, converter), primaryField.getStringValueConverter(), Objects::equals);
    }

    public boolean check(DataTable table, CurrentEntityState fetchedFields) {
        return fetchedFields.safeGet(cache.get(table)).isNullOrAbsent();
    }







}
