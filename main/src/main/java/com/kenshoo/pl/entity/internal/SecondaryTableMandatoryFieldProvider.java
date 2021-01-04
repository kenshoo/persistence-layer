package com.kenshoo.pl.entity.internal;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.CurrentEntityState;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.ValueConverter;
import com.kenshoo.pl.entity.converters.IdentityValueConverter;
import org.jooq.ForeignKey;
import org.jooq.Record;
import org.jooq.Table;
import org.jooq.TableField;

import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.kenshoo.pl.entity.AbstractEntityType.createStringValueConverter;

public class SecondaryTableMandatoryFieldProvider<E extends EntityType<E>> {

    private final E entityType;

    public SecondaryTableMandatoryFieldProvider(E entityType) {
        this.entityType = entityType;
    }

    public Collection<EntityField<?, ?>> get(DataTable secondaryTable) {
        final List<TableField<Record, ?>> foreignKeyFields = secondaryTable.getForeignKey(entityType.getPrimaryTable()).getFields();
        return foreignKeyFields.stream()
                .map(tableField -> entityType.findField(tableField).orElse(createEntityField(entityType, tableField)))
                .collect(Collectors.toList());
    }

    private <T> EntityField<E, ?> createEntityField(E entityType, TableField<Record, T> tableField) {
        final ValueConverter<T, T> converter = IdentityValueConverter.getInstance(tableField.getType());
        return new EntityFieldImpl<>(entityType, new SimpleEntityFieldDbAdapter<>(tableField, converter), createStringValueConverter(converter.getValueClass()), Objects::equals);
    }
}
