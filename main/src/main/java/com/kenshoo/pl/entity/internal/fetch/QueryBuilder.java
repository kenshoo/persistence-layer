package com.kenshoo.pl.entity.internal.fetch;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.kenshoo.jooq.*;
import com.kenshoo.pl.entity.*;
import org.jooq.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.jooq.impl.DSL.trueCondition;
import static org.jooq.lambda.function.Functions.not;


public class QueryBuilder<E extends EntityType<E>> {

    private final QueryExtender DONT_EXTEND_WITH_IDS = this::dontExtend;

    private DSLContext dslContext;
    private List<SelectField<?>> selectedFields;
    private DataTable startingTable;
    private List<TreeEdge> paths = Collections.emptyList();
    private Set<OneToOneTableRelation> oneToOneTableRelations = Collections.emptySet();
    private Condition condition = trueCondition();
    private Partitioner partitioner = this::addPartitionToCondition;
    private QueryExtender queryExtender = DONT_EXTEND_WITH_IDS;


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

    public QueryBuilder<E> whereIdsIn(Collection<? extends Identifier<? extends EntityType<?>>> ids) {
        if (queryExtender != DONT_EXTEND_WITH_IDS) {
            throw new IllegalStateException("We currently support only a single query extension");
        }
        this.queryExtender = query -> addIdCondition(query, (Collection)ids);
        return this;
    }

    public QueryBuilder<E> withCondition(Condition condition) {
        this.condition = condition;
        return this;
    }

    public QueryBuilder<E> withoutPartitions() {
        this.partitioner = NO_PARTITION;
        return this;
    }

    public QueryExtension<SelectFinalStep<Record>> build() {
        final SelectJoinStep<Record> query = dslContext.select(selectedFields).from(startingTable);
        final Set<DataTable> joinedTables = Sets.newHashSet(startingTable);
        paths.forEach(edge -> joinTables(query, joinedTables, edge));
        joinSecondaryTables(query, joinedTables, oneToOneTableRelations);
        condition = partitioner.transform(startingTable, condition);
        query.where(condition);

        return queryExtender.transform(query);
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
        Condition joinCondition = trueCondition();
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

    private Condition addPartitionToCondition(DataTable table, final Condition inputJooqCondition) {
        return table.getVirtualPartition().stream()
                .map(fv -> (FieldAndValue<Object>) fv)
                .map(fieldAndValue -> fieldAndValue.getField().eq(fieldAndValue.getValue()))
                .reduce(inputJooqCondition, Condition::and);
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

    private interface Partitioner {
        Condition transform(DataTable table, Condition condition);
    }

    private final Partitioner NO_PARTITION = (table, cond) -> cond;

    private interface QueryExtender {
        QueryExtension<SelectFinalStep<Record>> transform(SelectFinalStep<Record> query);
    }

    private QueryExtension<SelectFinalStep<Record>> dontExtend(SelectFinalStep<Record> query){
        return new QueryExtension<SelectFinalStep<Record>>() {
            @Override
            public SelectFinalStep<Record> getQuery() {
                return query;
            }

            @Override
            public void close() {
            }
        };
    }

    private <I extends EntityType<I>, ID extends Identifier<I>> QueryExtension<SelectFinalStep<Record>> addIdCondition(SelectFinalStep query, Collection<? extends ID> ids) {
        return SelectQueryExtender.of(this.dslContext, query, Identifier.groupValuesByFields(ids));
    }

}

