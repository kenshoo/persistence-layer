package com.kenshoo.pl.entity.internal.fetch;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.kenshoo.jooq.*;
import com.kenshoo.pl.data.ImpersonatorTable;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.UniqueKey;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.jooq.lambda.function.Functions.not;

public class QueryBuilder<E extends EntityType<E>> {

    private DSLContext dslContext;
    private List<SelectField<?>> selectedFields;
    private DataTable startingTable;
    private List<TreeEdge> paths;
    private Set<OneToOneTableRelation> oneToOneTableRelations;
    private Collection<? extends Identifier<E>> ids;


    public QueryBuilder(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public QueryBuilder<E> selecting(List<SelectField<?>> selectedFields) {
        this.selectedFields = selectedFields;
        return this;
    }

    public QueryBuilder<E> from(DataTable primaryTable) {
        this.startingTable = primaryTable;
        return this;
    }

    public QueryBuilder<E> innerJoin(List<TreeEdge> paths) {
        this.paths = paths;
        return this;
    }

    public QueryBuilder<E> innerJoin(TreeEdge path) {
        this.paths = Lists.newArrayList(path);
        return this;
    }

    public QueryBuilder<E> leftJoin(Set<OneToOneTableRelation> oneToOneTableRelations) {
        this.oneToOneTableRelations = oneToOneTableRelations;
        return this;
    }

    public QueryBuilder<E> whereIdsIn(Collection<? extends Identifier<E>> ids) {
        this.ids = ids;
        return this;
    }

    public QueryExtension<SelectJoinStep<Record>> build() {
        final SelectJoinStep<Record> query = dslContext.select(selectedFields).from(startingTable);
        final Set<DataTable> joinedTables = Sets.newHashSet(startingTable);
        paths.forEach(edge -> joinTables(query, joinedTables, edge));
        if (oneToOneTableRelations != null) {
            joinSecondaryTables(query, joinedTables, oneToOneTableRelations);
        }
        if (ids != null) {
            final UniqueKey<E> uniqueKey = ids.iterator().next().getUniqueKey();
            return addIdsCondition(query, startingTable, uniqueKey, ids);
        }
        return new QueryExtension<SelectJoinStep<Record>>() {
            @Override
            public SelectJoinStep<Record> getQuery() {
                return query;
            }

            @Override
            public void close() {
            }
        };
    }

    public <Q extends SelectFinalStep> QueryExtension<Q> addIdsCondition(Q query, DataTable primaryTable, UniqueKey<E> uniqueKey, Collection<? extends Identifier<E>> identifiers) {
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

    static void joinTables(SelectJoinStep<Record> query, Set<DataTable> alreadyJoinedTables, TreeEdge edgeInThePath) {
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

    static void joinSecondaryTables(SelectJoinStep<Record> query, Set<? extends Table<Record>> alreadyJoinedTables, Set<OneToOneTableRelation> targetOneToOneRelations) {
        targetOneToOneRelations.stream()
                .filter(not(secondaryTableIn(alreadyJoinedTables)))
                .forEach(addLeftJoinTo(query));
    }

    private static Condition getJoinCondition(Table<Record> fromTable, Table<Record> toTable) {
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

    private <T> void addToConditions(EntityField<E, T> field, Collection<? extends Identifier<E>> identifiers, List<FieldAndValues<?>> conditions) {
        EntityFieldDbAdapter<T> dbAdapter = field.getDbAdapter();
        List<Object> fieldValues = new ArrayList<>(identifiers.size());
        for (Identifier<E> identifier : identifiers) {
            dbAdapter.getDbValues(identifier.get(field)).sequential().forEach(fieldValues::add);
        }
        Optional<TableField<Record, ?>> tableField = dbAdapter.getTableFields().findFirst();
        conditions.add(new FieldAndValues<>((TableField<Record, Object>) tableField.get(), fieldValues));
    }

    private static Predicate<OneToOneTableRelation> secondaryTableIn(Set<? extends Table<Record>> joinedTables) {
        return relation -> joinedTables.contains(relation.getSecondary());
    }

    private static Consumer<OneToOneTableRelation> addLeftJoinTo(SelectJoinStep<Record> query) {
        return relation -> query.leftOuterJoin(relation.getSecondary()).on(getJoinCondition(relation));
    }

    private static Condition getJoinCondition(final OneToOneTableRelation relation) {
        return getJoinCondition(relation.getSecondary(), relation.getPrimary());
    }
}
