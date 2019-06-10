package com.kenshoo.pl.entity.spi.helpers;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.kenshoo.jooq.DataTable;
import com.kenshoo.jooq.TempTableHelper;
import com.kenshoo.jooq.TempTableResource;
import com.kenshoo.pl.data.ImpersonatorTable;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.FieldsValueMap;
import org.jooq.DSLContext;

import java.util.Collection;
import java.util.stream.Stream;

public class EntitiesTempTableCreator {

    private final DSLContext dslContext;

    public EntitiesTempTableCreator(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public <E extends EntityType<E>> TempTableResource<ImpersonatorTable> createTempTable(final Collection<? extends EntityField<E, ?>> fields, final Collection<? extends FieldsValueMap<E>> fieldsValueMaps) {
        Preconditions.checkArgument(!fields.isEmpty(), "fields is empty");
        //noinspection ConstantConditions
        DataTable primaryTable = Iterables.getFirst(fields, null).getDbAdapter().getTable();
        ImpersonatorTable impersonatorTable = new ImpersonatorTable(primaryTable);
        fields.stream().flatMap(field -> field.getDbAdapter().getTableFields()).forEach(impersonatorTable::createField);
        return TempTableHelper.tempInMemoryTable(dslContext, impersonatorTable, batchBindStep -> {
            fieldsValueMaps.forEach(entityChange -> batchBindStep.bind(fields.stream().flatMap(field -> getDbValues(entityChange, field)).toArray()));
        });
    }

    private <E extends EntityType<E>, T> Stream<Object> getDbValues(FieldsValueMap<E> fieldsValueMap, EntityField<E, T> field) {
        return field.getDbAdapter().getDbValues(fieldsValueMap.get(field));
    }
}
