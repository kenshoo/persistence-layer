package com.kenshoo.pl.entity.spi.helpers;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.kenshoo.pl.jooq.DataTable;
import com.kenshoo.pl.jooq.TempTableHelper;
import com.kenshoo.pl.jooq.TempTableResource;
import com.kenshoo.pl.data.ImpersonatorTable;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.FieldsValueMap;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.stream.Stream;

@Component
public class EntitiesTempTableCreator<E extends EntityType<E>> {

    @Resource
    private DSLContext dslContext;

    @Resource
    private TempTableHelper tempTableHelper;

    public TempTableResource<ImpersonatorTable> createTempTable(final Collection<? extends EntityField<E, ?>> fields, final Collection<? extends FieldsValueMap<E>> fieldsValueMaps) {
        Preconditions.checkArgument(!fields.isEmpty(), "fields is empty");
        //noinspection ConstantConditions
        DataTable primaryTable = Iterables.getFirst(fields, null).getDbAdapter().getTable();
        ImpersonatorTable impersonatorTable = new ImpersonatorTable(primaryTable);
        fields.stream().flatMap(field -> field.getDbAdapter().getTableFields()).forEach(impersonatorTable::createField);
        return tempTableHelper.tempInMemoryTable(dslContext, impersonatorTable, batchBindStep -> {
            fieldsValueMaps.forEach(entityChange -> batchBindStep.bind(fields.stream().flatMap(field -> getDbValues(entityChange, field)).toArray()));
        });
    }

    private <T> Stream<Object> getDbValues(FieldsValueMap<E> fieldsValueMap, EntityField<E, T> field) {
        return field.getDbAdapter().getDbValues(fieldsValueMap.get(field));
    }
}
