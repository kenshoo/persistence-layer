package com.kenshoo.pl.entity.internal.fetch;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.jooq.FieldAndValues;
import com.kenshoo.jooq.QueryExtension;
import com.kenshoo.jooq.SelectQueryExtender;
import com.kenshoo.pl.entity.UniqueKey;
import com.kenshoo.pl.entity.*;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.jooq.lambda.function.Functions.not;

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

    public void joinTables(SelectJoinStep<Record> query, Set<DataTable> alreadyJoinedTables, TreeEdge edgeInThePath) {
        // The joins must be composed in the order of traversal, so we have to "unwind" the path traveled from the root
        // Using a stack for that
        LinkedList<TreeEdge> joins = new LinkedList<>();
        joins.push(edgeInThePath);
        // Push onto the stack until we reach a table already joined (or the starting table)
        while (!alreadyJoinedTables.contains(edgeInThePath.source.table)) {
            edgeInThePath = edgeInThePath.source.parent;
            joins.push(edgeInThePath);
        }
        // Perform the joins
        for (TreeEdge join : joins) {
            DataTable rhs = join.target.table;
            query.join(rhs).on(getJoinCondition(join.source.table, join.target.table));
            alreadyJoinedTables.add(rhs);
        }
    }

    public void joinSecondaryTables(SelectJoinStep<Record> query, Set<? extends Table<Record>> alreadyJoinedTables, Set<OneToOneTableRelation> targetOneToOneRelations) {
        targetOneToOneRelations.stream()
                .filter(not(secondaryTableIn(alreadyJoinedTables)))
                .forEach(addLeftJoinTo(query));
    }

    public Condition getJoinCondition(Table<Record> fromTable, Table<Record> toTable) {
        List<ForeignKey<Record, Record>> foreignKeys = toTable.getReferencesTo(fromTable);
        if (foreignKeys.isEmpty()) {
            foreignKeys = fromTable.getReferencesTo(toTable);
        }
        if (foreignKeys.isEmpty()) {
            return null;
        }
        Condition joinCondition = DSL.trueCondition();
        ForeignKey<Record, Record> foreignKey = foreignKeys.get(0);
        org.jooq.UniqueKey<Record> key = foreignKey.getKey();
        List<TableField<Record, ?>> otherTableFields = key.getFields();
        for (int i = 0; i < foreignKey.getFields().size(); i++) {
            TableField<Record, ?> tableField = foreignKey.getFields().get(i);
            //noinspection unchecked
            joinCondition = joinCondition.and(tableField.eq((TableField) otherTableFields.get(i)));
        }
        return joinCondition;
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

    private Predicate<OneToOneTableRelation> secondaryTableIn(Set<? extends Table<Record>> joinedTables) {
        return relation -> joinedTables.contains(relation.getSecondary());
    }

    private Consumer<OneToOneTableRelation> addLeftJoinTo(SelectJoinStep<Record> query) {
        return relation -> query.leftOuterJoin(relation.getSecondary()).on(getJoinCondition(relation));
    }

    private Condition getJoinCondition(final OneToOneTableRelation relation) {
        return getJoinCondition(relation.getSecondary(), relation.getPrimary());
    }
}