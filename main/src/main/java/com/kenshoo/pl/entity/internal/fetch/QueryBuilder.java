package com.kenshoo.pl.entity.internal.fetch;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.jooq.FieldAndValues;
import com.kenshoo.jooq.QueryExtension;
import com.kenshoo.jooq.SelectQueryExtender;
import com.kenshoo.pl.entity.UniqueKey;
import com.kenshoo.pl.entity.*;
import org.jooq.*;

import java.util.*;

public class QueryBuilder {

    private final DSLContext dslContext;

    public QueryBuilder(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public <E extends EntityType<E>, Q extends SelectFinalStep> QueryExtension<Q> addIdsCondition(Q query, DataTable primaryTable, UniqueKey<E> uniqueKey, Collection<? extends Identifier<E>> identifiers) {
        List<FieldAndValues<?>> conditions = new ArrayList<>();
        for (EntityField<E, ?> field : uniqueKey.getFields()) {
            addToConditions(field, identifiers, conditions);
        }
        primaryTable.getVirtualPartition().forEach(fieldAndValue -> {
            Object[] values = new Object[identifiers.size()];
            Arrays.fill(values, fieldAndValue.getValue());
            conditions.add(new FieldAndValues<>((Field<Object>) fieldAndValue.getField(), Arrays.asList(values)));
        });
        return SelectQueryExtender.of(dslContext, query, conditions);
    }

    private <E extends EntityType<E>, T> void addToConditions(EntityField<E, T> field, Collection<? extends Identifier<E>> identifiers, List<FieldAndValues<?>> conditions) {
        EntityFieldDbAdapter<T> dbAdapter = field.getDbAdapter();
        List<Object> fieldValues = new ArrayList<>(identifiers.size());
        for (Identifier<E> identifier : identifiers) {
            dbAdapter.getDbValues(identifier.get(field)).sequential().forEach(fieldValues::add);
        }
        Optional<TableField<Record, ?>> tableField = dbAdapter.getTableFields().findFirst();
        conditions.add(new FieldAndValues<>((TableField<Record, Object>) tableField.get(), fieldValues));
    }
}
