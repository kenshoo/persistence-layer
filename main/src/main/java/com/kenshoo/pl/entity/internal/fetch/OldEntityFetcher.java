package com.kenshoo.pl.entity.internal.fetch;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.kenshoo.jooq.*;
import com.kenshoo.pl.data.ImpersonatorTable;
import com.kenshoo.pl.entity.UniqueKey;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.EntityImpl;
import com.kenshoo.pl.entity.internal.EntityTypeReflectionUtil;
import com.kenshoo.pl.entity.internal.PartialEntityInvocationHandler;
import org.jooq.*;
import org.jooq.lambda.Seq;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.function.Predicate;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;
import static org.apache.commons.lang3.Validate.notEmpty;
import static org.jooq.lambda.Seq.seq;
import static org.jooq.lambda.function.Functions.not;


public class OldEntityFetcher {

    private final DSLContext dslContext;

    private final QueryBuilderHelper queryBuilderHelper;

    public OldEntityFetcher(DSLContext dslContext) {
        this.dslContext = dslContext;
        this.queryBuilderHelper = new QueryBuilderHelper(dslContext);
    }

    public <E extends EntityType<E>> Map<Identifier<E>, Entity> fetchEntitiesByKeys(final E entityType,
                                                                                    final UniqueKey<E> uniqueKey,
                                                                                    final Collection<? extends Identifier<E>> keys,
                                                                                    final Collection<? extends EntityField<?, ?>> fieldsToFetch) {
        return fetchEntitiesByIds(keys, fieldsToFetch);

    }

    public <E extends EntityType<E>> Map<Identifier<E>, Entity> fetchEntitiesByIds(final Collection<? extends Identifier<E>> ids,
                                                                                   final EntityField<?, ?>... fieldsToFetchArgs) {
        return fetchEntitiesByIds(ids, ImmutableList.copyOf(fieldsToFetchArgs));
    }

    public <E extends EntityType<E>> Map<Identifier<E>, Entity> fetchEntitiesByIds(final Collection<? extends Identifier<E>> ids,
                                                                                   final Collection<? extends EntityField<?, ?>> fieldsToFetch) {
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }
        final UniqueKey<E> uniqueKey = ids.iterator().next().getUniqueKey();
        final EntityType<E> entityType = uniqueKey.getEntityType();
        final AliasedKey<E> aliasedKey = new AliasedKey<>(uniqueKey);

        final SelectJoinStep<Record> query = buildFetchQuery(entityType.getPrimaryTable(), aliasedKey.aliasedFields(), fieldsToFetch);
        try (QueryExtension<SelectJoinStep<Record>> queryExtender = queryBuilderHelper.addIdsCondition(query, entityType.getPrimaryTable(), uniqueKey, ids)) {
            return fetchEntitiesMap(queryExtender.getQuery(), aliasedKey, fieldsToFetch);
        }
    }

    public List<Entity> fetch(final EntityType<?> entityType,
                              final PLCondition plCondition,
                              final EntityField<?, ?>... fieldsToFetch) {
        requireNonNull(entityType, "An entity type must be provided");
        requireNonNull(plCondition, "A condition must be provided");
        notEmpty(fieldsToFetch, "There must be at least one field to fetch");

        final Set<EntityField<?, ?>> requestedFieldsToFetch = ImmutableSet.copyOf(fieldsToFetch);
        final Set<? extends EntityField<?, ?>> allFieldsToFetch = Sets.union(requestedFieldsToFetch, plCondition.getFields());

        final SelectJoinStep<Record> query = buildFetchQuery(entityType.getPrimaryTable(),
                emptyList(),
                allFieldsToFetch);
        final Condition completeJooqCondition = addVirtualPartitionConditions(entityType, plCondition.getJooqCondition());

        return query.where(completeJooqCondition)
                .fetch(record -> mapRecordToEntity(record, requestedFieldsToFetch));
    }

    public <E extends EntityType<E>> Map<Identifier<E>, Entity> fetchEntitiesByForeignKeys(E entityType, UniqueKey<E> foreignUniqueKey, Collection<? extends Identifier<E>> keys, Collection<EntityField<?, ?>> fieldsToFetch) {
        try (final TempTableResource<ImpersonatorTable> foreignKeysTable = createForeignKeysTable(entityType.getPrimaryTable(), foreignUniqueKey, keys)) {
            final AliasedKey<E> aliasedKey = new AliasedKey<>(foreignUniqueKey, foreignKeysTable);
            final SelectJoinStep<Record> query = buildFetchQuery(foreignKeysTable.getTable(), aliasedKey.aliasedFields(), fieldsToFetch);

            return fetchEntitiesMap(query, aliasedKey, fieldsToFetch);
        }
    }

    public <E extends EntityType<E>, PE extends PartialEntity, ID extends Identifier<E>> Map<ID, PE> fetchPartialEntities(E entityType, Collection<ID> keys, final Class<PE> entityIface) {
        if (keys.isEmpty()) {
            return Collections.emptyMap();
        }
        UniqueKey<E> uniqueKey = keys.iterator().next().getUniqueKey();
        final Map<Method, EntityField<E, ?>> entityMethodsMap = EntityTypeReflectionUtil.getMethodsMap(entityType, entityIface);
        Map<Identifier<E>, Entity> entityMap = fetchEntitiesByIds(keys, entityMethodsMap.values());
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

    /*
     This method generates a query that joins the starting table with one or more foreign keys, with the tables
     necessary to get to all fieldsToFetch. This is done by traversing the tree that starts at the starting table and
     follows all possible foreign keys, in the BFS manner. As soon as a table containing one of the fieldsToFetch is found,
     the necessary joins are made and the participating tables are marked as already joined. The target table is removed
     from the set of tables to fetch. The traversal continues until there are tables in this set.
     */
    private SelectJoinStep<Record> buildFetchQuery(DataTable startingTable, Collection<? extends Field<?>> aliasedKeyFields, Collection<? extends EntityField<?, ?>> fieldsToFetch) {
        // The set of tables to reach with joins. This set is mutable, the tables are removed from it as they are reached
        Set<DataTable> targetPrimaryTables = fieldsToFetch.stream()
                .map(field -> field.getEntityType().getPrimaryTable())
                .filter(tb -> !tb.equals(startingTable))
                .collect(toSet());

        final Set<OneToOneTableRelation> targetOneToOneRelations = fieldsToFetch.stream()
                .filter(not(isOfPrimaryTable()))
                .map(field -> OneToOneTableRelation.builder()
                        .secondary(field.getDbAdapter().getTable())
                        .primary(field.getEntityType().getPrimaryTable())
                        .build())
                .collect(toSet());

        List<SelectField<?>> selectFields = dbFieldsOf(fieldsToFetch).concat(seq(aliasedKeyFields)).toList();

        final SelectJoinStep<Record> query = dslContext.select(selectFields).from(startingTable);
        final Set<DataTable> joinedTables = Sets.newHashSet(startingTable);
        final TreeEdge startingEdge = new TreeEdge(null, startingTable);

        BFS.visit(startingEdge, this::edgesComingOutOf)
                .limitUntil(__ -> targetPrimaryTables.isEmpty())
                .forEach(edge -> {
                    final DataTable table = edge.target.table;

                    if (edge != startingEdge && targetPrimaryTables.contains(table)) {
                        targetPrimaryTables.remove(table);
                        queryBuilderHelper.joinTables(query, joinedTables, edge);
                    }
                });

        if (!targetPrimaryTables.isEmpty()) {
            throw new IllegalStateException("Tables " + targetPrimaryTables + " could not be reached via joins");
        }

        queryBuilderHelper.joinSecondaryTables(query, joinedTables, targetOneToOneRelations);

        return query;
    }

    private Predicate<EntityField<?, ?>> isOfPrimaryTable() {
        return field -> field.getDbAdapter().getTable().equals(field.getEntityType().getPrimaryTable());
    }

    private Seq<TreeEdge> edgesComingOutOf(TreeEdge edge) {
        return seq(edge.target.table.getReferences()).map(new ToEdgesOf(edge.target));
    }

    private <E extends EntityType<E>> Map<Identifier<E>, Entity> fetchEntitiesMap(ResultQuery<Record> query, AliasedKey<E> aliasedKey, Collection<? extends EntityField<?, ?>> fields) {
        return query.fetchMap(record -> RecordReader.createKey(record, aliasedKey), record -> RecordReader.createEntity(record, fields));
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

    private <T> void fieldFromRecordToEntity(EntityImpl entity, EntityField<?, T> field, Iterator<Object> valuesIterator) {
        entity.set(field, field.getDbAdapter().getFromRecord(valuesIterator));
    }

    private <E extends EntityType<E>, T> void addToValues(Identifier<E> key, EntityField<E, T> field, List<Object> values) {
        field.getDbAdapter().getDbValues(key.get(field)).forEach(values::add);
    }

    private Seq<SelectField<?>> dbFieldsOf(Collection<? extends EntityField<?, ?>> fieldsToFetch) {
        return seq(fieldsToFetch).flatMap(field -> field.getDbAdapter().getTableFields());
    }

    private Condition addVirtualPartitionConditions(final EntityType<?> entityType, final Condition inputJooqCondition) {
        return entityType.getPrimaryTable().getVirtualPartition().stream()
                         .map(this::asTypedFieldAndValue)
                         .map(fieldAndValue -> fieldAndValue.getField().eq(fieldAndValue.getValue()))
                         .reduce(inputJooqCondition, Condition::and);
    }

    private Entity mapRecordToEntity(final Record record, final Collection<EntityField<?, ?>> fieldsToFetch) {
        final EntityImpl entity = new EntityImpl();
        final Iterator<Object> valuesIterator = record.intoList().iterator();
        fieldsToFetch.forEach( field -> fieldFromRecordToEntity(entity, field, valuesIterator));
        return entity;
    }

    @SuppressWarnings("unchecked")
    private FieldAndValue<Object> asTypedFieldAndValue(final FieldAndValue<?> fv) {
        return (FieldAndValue<Object>)fv;
    }
}
