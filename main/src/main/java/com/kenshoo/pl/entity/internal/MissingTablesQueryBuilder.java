package com.kenshoo.pl.entity.internal;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.internal.fetch.TreeEdge;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.jooq.lambda.function.Functions.not;


public class MissingTablesQueryBuilder {

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
