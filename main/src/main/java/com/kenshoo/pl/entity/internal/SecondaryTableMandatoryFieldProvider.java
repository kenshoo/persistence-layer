package com.kenshoo.pl.entity.internal;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.ValueConverter;
import com.kenshoo.pl.entity.converters.IdentityValueConverter;
import org.jooq.ForeignKey;
import org.jooq.Record;
import org.jooq.TableField;

public class SecondaryTableMandatoryFieldProvider<E extends EntityType<E>> {

    private final E entityType;

    public SecondaryTableMandatoryFieldProvider(E entityType) {
        this.entityType = entityType;
    }

    public EntityField<E, ?> get(DataTable secondaryTable) {
        final ForeignKey<Record, Record> foreignKey = secondaryTable.getForeignKey(entityType.getPrimaryTable());
        final TableField<?, ?> primaryField = foreignKey.getKey().getFields().get(0);
        final TableField<Record, ?> secondaryField = foreignKey.getFields().get(0);
        return entityType.findField(secondaryField).orElse(createEntityField(secondaryField, primaryField));
    }

    private <T> EntityField<E, ?> createEntityField(TableField<Record, T> secondaryField, TableField<?, ?> primaryTableField) {
        final EntityField<E, ?> primaryField = entityType.findField(primaryTableField).get();
        final ValueConverter<T, T> converter = IdentityValueConverter.getInstance(secondaryField.getType());
        return new EntityFieldImpl(entityType, new SimpleEntityFieldDbAdapter<>(secondaryField, converter), primaryField.getStringValueConverter(), primaryField.getValueEqualityFunction());
    }
}
