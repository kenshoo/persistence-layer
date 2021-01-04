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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.kenshoo.pl.entity.AbstractEntityType.createStringValueConverter;

public class SecondaryTableMandatoryFieldProvider {

    private Map<DataTable, Collection<? extends EntityField<?, ?>>> keysToPrimary;


    public <E extends EntityType<E>> Stream<? extends EntityField<?, ?>> getForeignFields(E entityType, Collection<? extends EntityField<E, ?>> fieldsToUpdate) {
        this.keysToPrimary = fieldsToUpdate.stream()
                .filter(field -> !field.getDbAdapter().getTable().equals(entityType.getPrimaryTable()))
                .collect(Collectors.toMap(field -> field.getDbAdapter().getTable(), field -> createEntityFields(entityType, field), (k1, k2) -> k1));
        return this.keysToPrimary.values().stream().flatMap(Collection::stream);
    }

    public boolean shouldCreateEntity(CurrentEntityState entity, DataTable secondaryTable) {
        return this.keysToPrimary.get(secondaryTable).stream()
                .anyMatch(field -> entity.safeGet(field).isAbsent());
    }


    private <E extends EntityType<E>> Collection<EntityField<?, ?>> createEntityFields(E entityType, EntityField<E, ?> field) {
        final List<TableField<Record, ?>> foreignKeyFields = field.getDbAdapter().getTable().getForeignKey(entityType.getPrimaryTable()).getFields();
        return foreignKeyFields.stream()
                .map(tableField -> createEntityField(entityType, tableField))
                .collect(Collectors.toList());
    }

    private <E extends EntityType<E>, T> EntityField<?, ?> createEntityField(E entityType, TableField<Record, T> tableField) {
        final ValueConverter<T, T> converter = IdentityValueConverter.getInstance(tableField.getType());
        return new EntityFieldImpl<>(entityType, new SimpleEntityFieldDbAdapter<>(tableField, converter), createStringValueConverter(converter.getValueClass()), Objects::equals);
    }


}
