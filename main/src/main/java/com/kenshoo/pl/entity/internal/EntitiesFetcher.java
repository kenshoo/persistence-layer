package com.kenshoo.pl.entity.internal;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.kenshoo.jooq.*;
import com.kenshoo.pl.BetaTesting;
import com.kenshoo.pl.data.ImpersonatorTable;
import com.kenshoo.pl.entity.UniqueKey;
import com.kenshoo.pl.entity.*;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.lambda.Seq;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.kenshoo.pl.BetaTesting.Feature.FindSecondaryTablesOfParents;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;
import static org.jooq.lambda.Seq.seq;

public class EntitiesFetcher {

    private final DSLContext dslContext;

    public EntitiesFetcher(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public <E extends EntityType<E>> Map<Identifier<E>, Entity> fetchEntitiesByKeys(E entityType, UniqueKey<E> uniqueKey, Collection<? extends Identifier<E>> keys, Collection<? extends EntityField<?, ?>> fieldsToFetch) {
        if (keys.isEmpty()) {
            return Collections.emptyMap();
        }

        //noinspection ConstantConditions
        SelectJoinStep<Record> query = buildFetchQuery(entityType.getPrimaryTable(), uniqueKey.getTableFields(), fieldsToFetch);
        try (QueryExtension<SelectJoinStep<Record>> queryExtender = queryExtender(query, entityType.getPrimaryTable(), uniqueKey, keys)) {
            return fetchEntitiesMap(queryExtender.getQuery(), uniqueKey, fieldsToFetch);
        }
    }

    private <E extends EntityType<E>, Q extends SelectFinalStep> QueryExtension<Q> queryExtender(Q query, DataTable primaryTable, UniqueKey<E> uniqueKey, Collection<? extends Identifier<E>> identifiers) {
        List<FieldAndValues<?>> conditions = new ArrayList<>();
        for (EntityField<E, ?> field : uniqueKey.getFields()) {
            addToConditions(field, identifiers, conditions);
        }
        primaryTable.getVirtualPartition().forEach(fieldAndValue -> {
            Object[] values = new Object[identifiers.size()];
            Arrays.fill(values, fieldAndValue.getValue());
            //noinspection unchecked
            conditions.add(new FieldAndValues<>((Field<Object>) fieldAndValue.getField(), Arrays.asList(values)));
        });
        return SelectQueryExtender.of(dslContext, query, conditions);
    }

    private <E extends EntityType<E>, T> void addToConditions(EntityField<E, T> field, Collection<? extends Identifier<E>> identifiers, List<FieldAndValues<?>> conditions) {
        EntityFieldDbAdapter<T> dbAdapter = field.getDbAdapter();
        List<Object> fieldValues = new ArrayList<>(identifiers.size());
        for (Identifier<E> identifier : identifiers) {
            //noinspection unchecked
            dbAdapter.getDbValues(identifier.get(field)).sequential().forEach(fieldValues::add);
        }
        Optional<TableField<Record, ?>> tableField = dbAdapter.getTableFields().findFirst();
        //noinspection unchecked
        conditions.add(new FieldAndValues<>((TableField<Record, Object>) tableField.get(), fieldValues));
    }

    public <E extends EntityType<E>> Map<Identifier<E>, Entity> fetchEntitiesByForeignKeys(E entityType, UniqueKey<E> foreignUniqueKey, Collection<? extends Identifier<E>> keys, Collection<EntityField<?, ?>> fieldsToFetch) {
        try (final TempTableResource<ImpersonatorTable> foreignKeysTable = createForeignKeysTable(entityType.getPrimaryTable(), foreignUniqueKey, keys)) {
            List<TableField<Record, ?>> keyFields = foreignUniqueKey.getTableFields().stream()
                    .map(field -> foreignKeysTable.getTable().getField(field))
                    .collect(toList());
            SelectJoinStep<Record> query = buildFetchQuery(foreignKeysTable.getTable(), keyFields, fieldsToFetch);
            //noinspection unchecked
            return fetchEntitiesMap(query, foreignUniqueKey, fieldsToFetch);
        }
    }

    public <E extends EntityType<E>, PE extends PartialEntity, ID extends Identifier<E>> Map<ID, PE> fetchPartialEntities(E entityType, Collection<ID> keys, final Class<PE> entityIface) {
        if (keys.isEmpty()) {
            return Collections.emptyMap();
        }
        UniqueKey<E> uniqueKey = keys.iterator().next().getUniqueKey();
        final Map<Method, EntityField<E, ?>> entityMethodsMap = EntityTypeReflectionUtil.getMethodsMap(entityType, entityIface);
        Map<Identifier<E>, Entity> entityMap = fetchEntitiesByKeys(entityType, uniqueKey, keys, entityMethodsMap.values());
        ClassLoader classLoader = entityIface.getClassLoader();
        Class<?>[] interfaces = {entityIface};
        //noinspection unchecked
        return keys.stream().filter(entityMap::containsKey)
                .collect(toMap(identity(), key -> (PE) Proxy.newProxyInstance(classLoader, interfaces, new PartialEntityInvocationHandler<>(entityMethodsMap, entityMap.get(key)))));
    }

    public <E extends EntityType<E>, PE extends PartialEntity> List<PE> fetchByCondition(E entityType, Condition condition, final Class<PE> entityIface) {
        final Map<Method, EntityField<E, ?>> entityMethodsMap = EntityTypeReflectionUtil.getMethodsMap(entityType, entityIface);
        final Collection<EntityField<E, ?>> fieldsToFetch = entityMethodsMap.values();
        SelectJoinStep<Record> query = buildFetchQuery(entityType.getPrimaryTable(), Collections.<Field<?>>emptyList(), fieldsToFetch);
        for (FieldAndValue fieldAndValue : entityType.getPrimaryTable().getVirtualPartition()) {
            //noinspection unchecked
            condition = condition.and(fieldAndValue.getField().eq(fieldAndValue.getValue()));
        }
        List<Entity> entities = query.where(condition).fetch(record -> {
            EntityImpl entity = new EntityImpl();
            Iterator<Object> valuesIterator = record.intoList().iterator();
            for (EntityField<E, ?> field : fieldsToFetch) {
                fieldFromRecordToEntity(entity, field, valuesIterator);
            }
            return entity;
        });
        //noinspection unchecked
        ClassLoader classLoader = entityIface.getClassLoader();
        Class<?>[] interfaces = {entityIface};
        //noinspection unchecked
        return entities.stream()
                .map(entity -> (PE) Proxy.newProxyInstance(classLoader, interfaces, new PartialEntityInvocationHandler<>(entityMethodsMap, entity)))
                .collect(toList());
    }

    private SelectJoinStep<Record> buildFetchQuery(DataTable startingTable, Collection<? extends Field<?>> keyFields, Collection<? extends EntityField<?, ?>> fieldsToFetch) {
        return BetaTesting.isEnabled(FindSecondaryTablesOfParents)
                ? buildFetchQuery_NEW(startingTable, keyFields, fieldsToFetch)
                : buildFetchQuery_DEPRECATED(startingTable, keyFields, fieldsToFetch);
    }

    /*
     This method generates a query that joins the starting table with one or more foreign keys, with the tables
     necessary to get to all fieldsToFetch. This is done by traversing the tree that starts at the starting table and
     follows all possible foreign keys, in the BFS manner. As soon as a table containing one of the fieldsToFetch is found,
     the necessary joins are made and the participating tables are marked as already joined. The target table is removed
     from the set of tables to fetch. The traversal continues until there are tables in this set.
     */
    private SelectJoinStep<Record> buildFetchQuery_NEW(DataTable startingTable, Collection<? extends Field<?>> keyFields, Collection<? extends EntityField<?, ?>> fieldsToFetch) {
        // The set of tables to reach with joins. This set is mutable, the tables are removed from it as they are reached
        Set<DataTable> targetTables = fieldsToFetch.stream()
                .map(field -> field.getDbAdapter().getTable())
                .filter(tb -> !tb.equals(startingTable))
                .collect(toSet());
        Collection<SelectField<?>> selectFields = fieldsToFetch.stream()
                .flatMap(field -> field.getDbAdapter().getTableFields())
                .collect(toList());
        int keyFieldIndex = 0;
        for (Field keyField : keyFields) {
            selectFields.add(keyField.as(keyFieldAlias(keyFieldIndex)));
            keyFieldIndex++;
        }
        final SelectJoinStep<Record> query = dslContext.select(selectFields).from(startingTable);
        final Set<DataTable> joinedTables = Sets.newHashSet(startingTable);
        final TreeEdge startingEdge = new TreeEdge(null, startingTable);

        BFS.visit(startingEdge, edge -> edgesComingOutOf(edge))
                .limitUntil(__ -> targetTables.isEmpty())
                .forEach(edge -> {
                    final DataTable table = edge.target.table;
                    final List<DataTable> secondaryTables = seq(targetTables).filter(hasReferenceTo(table)).toList();

                    targetTables.removeAll(secondaryTables);
                    if (edge != startingEdge && (targetTables.contains(table) || !secondaryTables.isEmpty())) {
                        targetTables.remove(table);
                        joinMissingTablesInPath(query, joinedTables, edge);
                    }
                    addToJoin(query, table, secondaryTables);
                });

        if (!targetTables.isEmpty()) {
            throw new IllegalStateException("Tables " + targetTables + " could not be reached via joins");
        }

        return query;
    }

    private Seq<TreeEdge> edgesComingOutOf(TreeEdge edge) {
        return seq(edge.target.table.getReferences()).map(new ToEdgesOf(edge.target));
    }

    private void joinMissingTablesInPath(SelectJoinStep<Record> query, Set<DataTable> tablesAlreadyJoined, TreeEdge edgeInThePath) {
        // The joins must be composed in the order of traversal, so we have to "unwind" the path traveled from the root
        // Using a stack for that
        LinkedList<TreeEdge> joins = new LinkedList<>();
        joins.push(edgeInThePath);
        // Push onto the stack until we reach a table already joined (or the starting table)
        while (!tablesAlreadyJoined.contains(edgeInThePath.source.table)) {
            edgeInThePath = edgeInThePath.source.parent;
            joins.push(edgeInThePath);
        }
        // Perform the joins
        for (TreeEdge join : joins) {
            DataTable rhs = join.target.table;
            //noinspection unchecked
            query.join(rhs).on(getJoinCondition(join.source.table, join.target.table));
            tablesAlreadyJoined.add(rhs);
        }
    }

    private Predicate<DataTable> hasReferenceTo(DataTable toTable) {
        return testedTable -> !testedTable.getReferencesTo(toTable).isEmpty();
    }

    private SelectJoinStep<Record> addToJoin(SelectJoinStep<Record> query, DataTable startingTable, Iterable<DataTable> secondaryTables) {
        for (DataTable secondaryTable : secondaryTables) {
            query = query.leftOuterJoin(secondaryTable).on(getJoinCondition(secondaryTable, startingTable));
        }
        return query;
    }

    private static String keyFieldAlias(int keyFieldIndex) {
        return "key_field_" + keyFieldIndex;
    }

    private Condition getJoinCondition(Table<Record> fromTable, Table<Record> toTable) {
        List<ForeignKey<Record, Record>> foreignKeys = fromTable.getReferencesTo(toTable);
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

    private <E extends EntityType<E>> Map<Identifier<E>, Entity> fetchEntitiesMap(ResultQuery<Record> query, final UniqueKey<E> uniqueKey, final Collection<? extends EntityField<?, ?>> fields) {
        Map<Identifier<E>, Entity> entitiesMap = new HashMap<>();
        query.fetchInto(record -> {
            EntityField<E, ?>[] uniqueKeyFields = uniqueKey.getFields();
            FieldsValueMapImpl<E> key = new FieldsValueMapImpl<>();
            int keyFieldIndex = 0;
            for (EntityField<E, ?> field : uniqueKeyFields) {
                setKeyField(key, field, record.getValue(keyFieldAlias(keyFieldIndex)));
                keyFieldIndex++;
            }

            EntityImpl entity = new EntityImpl();
            Iterator<Object> valuesIterator = record.intoList().iterator();
            for (EntityField<?, ?> field : fields) {
                fieldFromRecordToEntity(entity, field, valuesIterator);
            }
            entitiesMap.put(uniqueKey.createValue(key), entity);
        });
        return entitiesMap;
    }

    private <E extends EntityType<E>, T> void setKeyField(FieldsValueMapImpl<E> key, EntityField<E, T> field, Object value) {
        T fieldValue = field.getDbAdapter().getFromRecord(Iterators.singletonIterator(value));
        key.set(field, fieldValue);
    }

    private <T> void fieldFromRecordToEntity(EntityImpl entity, EntityField<?, T> field, Iterator<Object> valuesIterator) {
        entity.set(field, field.getDbAdapter().getFromRecord(valuesIterator));
    }

    private <E extends EntityType<E>> TempTableResource<ImpersonatorTable> createForeignKeysTable(final DataTable primaryTable, final UniqueKey<E> foreignUniqueKey, final Collection<? extends Identifier<E>> keys) {
        ImpersonatorTable impersonatorTable = new ImpersonatorTable(primaryTable);
        foreignUniqueKey.getTableFields().forEach(impersonatorTable::createField);

        return TempTableHelper.tempInMemoryTable(dslContext, impersonatorTable, batchBindStep -> {
                    for (Identifier<E> key : keys) {
                        EntityField<E, ?>[] keyFields = foreignUniqueKey.getFields();
                        List<Object> values = new ArrayList<>();
                        for (EntityField<E, ?> field : keyFields) {
                            addToValues(key, field, values);
                        }
                        batchBindStep.bind(values.toArray());
                    }
                }
        );
    }

    private <E extends EntityType<E>, T> void addToValues(Identifier<E> key, EntityField<E, T> field, List<Object> values) {
        field.getDbAdapter().getDbValues(key.get(field)).forEach(values::add);
    }

    private static class TreeNode {
        private final DataTable table;
        private final TreeEdge parent;

        private TreeNode(TreeEdge parent, DataTable table) {
            this.parent = parent;
            this.table = table;
        }
    }

    private static class TreeEdge {
        private final TreeNode source;
        private final TreeNode target;

        private TreeEdge(TreeNode source, DataTable targetTable) {
            this.source = source;
            this.target = new TreeNode(this, targetTable);
        }
    }

    private static class ToEdgesOf implements Function<ForeignKey<Record, ?>, TreeEdge> {

        private final TreeNode node;

        public ToEdgesOf(TreeNode node) {
            this.node = node;
        }

        @Override
        public TreeEdge apply(ForeignKey<Record, ?> foreignKey) {
            return new TreeEdge(node, (DataTable) foreignKey.getTable());
        }

    }

    // -------------------------- DEPRECATED CODE ---------------------//

    private SelectJoinStep<Record> buildFetchQuery_DEPRECATED(DataTable startingTable, Collection<? extends Field<?>> keyFields, Collection<? extends EntityField<?, ?>> fieldsToFetch) {
        // The set of tables to reach with joins. This set is mutable, the tables are removed from it as they are reached
        Set<DataTable> tablesToFetch = fieldsToFetch.stream()
                .map(field -> field.getDbAdapter().getTable())
                .collect(toSet());
        Collection<SelectField<?>> selectFields = fieldsToFetch.stream()
                .flatMap(field -> field.getDbAdapter().getTableFields())
                .collect(toList());
        int keyFieldIndex = 0;
        for (Field keyField : keyFields) {
            selectFields.add(keyField.as(keyFieldAlias(keyFieldIndex)));
            keyFieldIndex++;
        }
        SelectJoinStep<Record> query = dslContext.select(selectFields).from(startingTable);

        // First, add left-joins for secondary tables of entity for the update flow. In create flow this loop won't find anything to join
        Iterator<DataTable> tablesToFetchIterator = tablesToFetch.iterator();
        while (tablesToFetchIterator.hasNext()) {
            DataTable table = tablesToFetchIterator.next();
            if (table.equals(startingTable)) {
                tablesToFetchIterator.remove();
                continue;
            }
            Condition joinCondition = getJoinCondition(table, startingTable);
            if (joinCondition == null) {
                continue;
            }
            //noinspection unchecked
            query = query.leftOuterJoin(table).on(joinCondition);
            tablesToFetchIterator.remove();
        }

        // The set of tables reached by BFS
        Set<DataTable> tablesReached = Sets.newHashSet();
        // The set of tables added to the resulting query
        Set<DataTable> joinedTables = Sets.newHashSet(startingTable);
        // The queue of BFS
        LinkedList<TreeEdge> edgesQueue = new LinkedList<>();
        TreeNode root = new TreeNode(null, startingTable);
        startingTable.getReferences().stream().map(new ToEdgesOf(root)).collect(toCollection(() -> edgesQueue));

        while (!tablesToFetch.isEmpty()) {
            TreeEdge treeEdge = edgesQueue.poll();
            if (treeEdge == null) {
                // The BFS queue is empty but there are still tables we didn't reach
                throw new IllegalStateException("Table " + tablesToFetch.iterator().next() + " could not be reached via joins");
            }
            DataTable joinTarget = treeEdge.target.table;
            if (tablesReached.contains(joinTarget)) {
                // If we have already reached this table by a different path, ignore it
                continue;
            }
            tablesReached.add(joinTarget);

            // Feed the BFS queue for the next time
            joinTarget.getReferences().stream().map(new ToEdgesOf(treeEdge.target)).collect(toCollection(() -> edgesQueue));

            // If we reached a table we don't need for the fieldsToFetch, just continue
            if (!tablesToFetch.contains(joinTarget)) {
                continue;
            }
            tablesToFetch.remove(joinTarget);

            // The joins must be composed in the order of traversal, so we have to "unwind" the path traveled from the root
            // Using a stack for that
            joinMissingTablesInPath(query, joinedTables, treeEdge);
        }
        return query;
    }

}
