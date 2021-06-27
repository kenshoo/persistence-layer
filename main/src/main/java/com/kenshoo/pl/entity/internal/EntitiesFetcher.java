package com.kenshoo.pl.entity.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.kenshoo.jooq.DataTable;
import com.kenshoo.jooq.QueryExtension;
import com.kenshoo.jooq.TempTableHelper;
import com.kenshoo.jooq.TempTableResource;
import com.kenshoo.pl.data.ImpersonatorTable;
import com.kenshoo.pl.entity.UniqueKey;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.fetch.AliasedKey;
import com.kenshoo.pl.entity.internal.fetch.ExecutionPlan;
import com.kenshoo.pl.entity.internal.fetch.QueryBuilder;
import com.kenshoo.pl.entity.internal.fetch.RecordReader;
import org.jooq.*;
import org.jooq.lambda.Seq;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.Validate.notEmpty;
import static org.jooq.impl.DSL.count;
import static org.jooq.lambda.Seq.seq;


public class EntitiesFetcher {

    private final DSLContext dslContext;
    private final FeatureSet features;

    public EntitiesFetcher(DSLContext dslContext) {
        this(dslContext, FeatureSet.EMPTY);
    }

    public EntitiesFetcher(DSLContext dslContext, FeatureSet features) {
        this.dslContext = dslContext;
        this.features = features;
    }

    public <E extends EntityType<E>> Map<Identifier<E>, CurrentEntityState> fetchEntitiesByIds(final Collection<? extends Identifier<E>> ids,
                                                                                               final EntityField<?, ?>... fieldsToFetchArgs) {
        return fetchEntitiesByIds(ids, ImmutableList.copyOf(fieldsToFetchArgs));
    }

    public <E extends EntityType<E>> Map<Identifier<E>, CurrentEntityState> fetchEntitiesByIds(final Collection<? extends Identifier<E>> ids,
                                                                                               final Collection<? extends EntityField<?, ?>> fieldsToFetch) {
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }

        final IdentifierType<E> uniqueKey = ids.iterator().next().getUniqueKey();
        final AliasedKey<E> aliasedKey = new AliasedKey<>(uniqueKey);

        return fetchEntities(uniqueKey.getEntityType().getPrimaryTable(), aliasedKey, fieldsToFetch, query -> query.whereIdsIn(ids));
    }

    public <E extends EntityType<E>> List<CurrentEntityState> fetch(final EntityType<E> entityType,
                                                                    final PLCondition plCondition,
                                                                    final EntityField<?, ?>... fieldsToFetch) {

        requireNonNull(entityType, "An entity type must be provided");
        requireNonNull(plCondition, "A condition must be provided");
        notEmpty(fieldsToFetch, "There must be at least one field to fetch");

        final AliasedKey<E> aliasedKey = new AliasedKey<>(entityType.getPrimaryKey());

        final List<? extends EntityField<?, ?>> allFieldsToFetch = Seq.concat(Seq.of(fieldsToFetch), seq(plCondition.getFields())).distinct().collect(Collectors.toList());

        return Lists.newArrayList(fetchEntities(entityType.getPrimaryTable(), aliasedKey, allFieldsToFetch, query -> query.withCondition(plCondition.getJooqCondition())).values());
    }

    public List<CurrentEntityState> fetch(final EntityType<?> entityType,
                                          final Collection<? extends Identifier<?>> keys,
                                          final PLCondition plCondition,
                                          final EntityField<?, ?>... fieldsToFetch) {
        requireNonNull(entityType, "An entity type must be provided");
        notEmpty(keys, "There must be at least one keys to fetch");
        requireNonNull(plCondition, "A condition must be provided");
        notEmpty(fieldsToFetch, "There must be at least one field to fetch");

        final AliasedKey<?> aliasedKey = new AliasedKey<>(entityType.getPrimaryKey());

        final List<? extends EntityField<?, ?>> allFieldsToFetch = Seq.concat(Seq.of(fieldsToFetch), seq(plCondition.getFields()), fieldsOf(keys)).distinct().collect(Collectors.toList());

        return Lists.newArrayList(fetchEntities(entityType.getPrimaryTable(), aliasedKey, allFieldsToFetch, query -> query.whereIdsIn(keys).withCondition(plCondition.getJooqCondition())).values());
    }

    public <E extends EntityType<E>> Map<Identifier<E>, Integer> fetchCount(final E entityType,
                                                                            final Collection<? extends Identifier<E>> ids,
                                                                            final PLCondition plCondition) {
        requireNonNull(entityType, "An entity type must be provided");
        notEmpty(ids, "There must be at least one id to fetch");
        requireNonNull(plCondition, "A condition must be provided");

        final var uniqueKey = ids.iterator().next().getUniqueKey();

        final var allFieldsToFetch = Seq.concat(seq(plCondition.getFields()), Stream.of(uniqueKey.getFields())).distinct().toList();

        final var executionPlan = new ExecutionPlan(entityType.getPrimaryTable(), allFieldsToFetch).getOneToOnePlan();

        final Field<Integer> countField = count();

        final var queryBuilder = new QueryBuilder<E>(dslContext)
                .selecting(dbFieldsOf(allFieldsToFetch).concat(countField).toList())
                .from(entityType.getPrimaryTable())
                .innerJoin(executionPlan.getPaths())
                .leftJoin(executionPlan.getSecondaryTableRelations())
                .whereIdsIn(ids)
                .withCondition(plCondition.getJooqCondition());

        try (var queryExtender = queryBuilder.build()) {
            return fetchCount(queryExtender.getQuery(), uniqueKey, countField);
        }
    }

    public <E extends EntityType<E>> Map<Identifier<E>, CurrentEntityState> fetchEntitiesByForeignKeys(E entityType, UniqueKey<E> foreignUniqueKey, Collection<? extends Identifier<E>> keys, Collection<EntityField<?, ?>> fieldsToFetch) {

        try (final TempTableResource<ImpersonatorTable> foreignKeysTable = createForeignKeysTable(entityType.getPrimaryTable(), foreignUniqueKey, keys)) {
            final AliasedKey<E> aliasedKey = new AliasedKey<>(foreignUniqueKey, foreignKeysTable);
            final DataTable startingTable = foreignKeysTable.getTable();

            return fetchEntities(startingTable, aliasedKey, fieldsToFetch, QueryBuilder::withoutPartitions);
        }
    }

    public <E extends EntityType<E>, PE extends PartialEntity, ID extends Identifier<E>> Map<ID, PE> fetchPartialEntities(E entityType, Collection<ID> ids, final Class<PE> entityIface) {

        final Map<Method, EntityField<E, ?>> entityMethodsMap = EntityTypeReflectionUtil.getMethodsMap(entityType, entityIface);
        Map<Identifier<E>, CurrentEntityState> entitiesMap = fetchEntitiesByIds(ids, entityMethodsMap.values());

        return seq(ids).filter(entitiesMap::containsKey).collect(toMap(identity(), id -> createInstance(entityIface, entityMethodsMap, entitiesMap.get(id))));
    }

    public <E extends EntityType<E>, PE extends PartialEntity> List<PE> fetchByCondition(E entityType, final Condition condition, final Class<PE> entityIface) {

        final Map<Method, EntityField<E, ?>> entityMethodsMap = EntityTypeReflectionUtil.getMethodsMap(entityType, entityIface);
        final AliasedKey<?> aliasedKey = new AliasedKey<>(entityType.getPrimaryKey());

        Collection<CurrentEntityState> entities = fetchEntities(entityType.getPrimaryTable(), aliasedKey, entityMethodsMap.values(), query -> query.withCondition(condition)).values();

        return entities.stream()
                .map(entity -> createInstance(entityIface, entityMethodsMap, entity))
                .collect(toList());
    }

    private <E extends EntityType<E>> Map<Identifier<E>, CurrentEntityState> fetchEntities(
            final DataTable startingTable,
            final AliasedKey<E> aliasedKey,
            final Collection<? extends EntityField<?, ?>> fieldsToFetch,
            Consumer<QueryBuilder<E>> queryModifier
    ) {
        final ExecutionPlan executionPlan = new ExecutionPlan(startingTable, fieldsToFetch);
        final ExecutionPlan.OneToOnePlan oneToOnePlan = executionPlan.getOneToOnePlan();

        final QueryBuilder<E> mainQueryBuilder = new QueryBuilder<E>(dslContext).selecting(selectFieldsOf(oneToOnePlan.getFields(), aliasedKey))
                .from(startingTable)
                .innerJoin(oneToOnePlan.getPaths())
                .leftJoin(oneToOnePlan.getSecondaryTableRelations());
        queryModifier.accept(mainQueryBuilder);

        final Map<Identifier<E>, CurrentEntityState> entities = fetchMainEntities(aliasedKey, oneToOnePlan, mainQueryBuilder);

        executionPlan.getManyToOnePlans().forEach(plan -> {
            final QueryBuilder<E> subQueryBuilder = new QueryBuilder<E>(dslContext).selecting(selectFieldsOf(plan.getFields(), aliasedKey))
                    .from(startingTable)
                    .innerJoin(plan.getPath());
            queryModifier.accept(subQueryBuilder);

            fetchAndPopulateSubEntities(aliasedKey, entities, plan, subQueryBuilder);
        });

        return entities;
    }

    private <E extends EntityType<E>, SUB extends EntityType<SUB>> void fetchAndPopulateSubEntities(AliasedKey<E> aliasedKey, Map<Identifier<E>, CurrentEntityState> entities, ExecutionPlan.ManyToOnePlan<SUB> plan, QueryBuilder<E> queryBuilder) {
        try (QueryExtension<SelectFinalStep<Record>> queryExtender = queryBuilder.build()) {
            Map<Identifier<E>, List<FieldsValueMap<SUB>>> multiValuesMap = fetchMultiValuesMap(queryExtender.getQuery(), aliasedKey, plan.getFields());
            multiValuesMap.forEach((Identifier<E> id, List<FieldsValueMap<SUB>> multiValues) -> {
                final SUB subEntityType = entityTypeOf(plan.getFields());
                ((CurrentEntityMutableState) entities.get(id)).add(subEntityType, multiValues);
            });
        }
    }

    private <E extends EntityType<E>> Map<Identifier<E>, CurrentEntityState> fetchMainEntities(AliasedKey<E> aliasedKey, ExecutionPlan.OneToOnePlan oneToOnePlan, QueryBuilder<E> queryBuilder) {
        try (QueryExtension<SelectFinalStep<Record>> queryExtender = queryBuilder.build()) {
            return fetchEntitiesMap(queryExtender.getQuery(), aliasedKey, oneToOnePlan.getFields());
        }
    }

    private <E extends EntityType<E>> Map<Identifier<E>, CurrentEntityState> fetchEntitiesMap(ResultQuery<Record> query, AliasedKey<E> aliasedKey, List<? extends EntityField<?, ?>> fields) {
        return query.fetchMap(record -> RecordReader.createKey(record, aliasedKey), record -> RecordReader.createEntity(record, fields));
    }

    private <E extends EntityType<E>> Map<Identifier<E>, Integer> fetchCount(ResultQuery<Record> query, IdentifierType<E> key, Field<Integer> count) {
        return query.fetchMap(record -> RecordReader.createKey(record, new AliasedKey<>(key)), record -> record.get(count));
    }

    private <MAIN extends EntityType<MAIN>, SUB extends EntityType<SUB>> Map<Identifier<MAIN>, List<FieldsValueMap<SUB>>> fetchMultiValuesMap(ResultQuery<Record> query, AliasedKey<MAIN> aliasedKey, List<? extends EntityField<SUB, ?>> fields) {
        final Map<Identifier<MAIN>, List<FieldsValueMap<SUB>>> multiValuesMap = new HashMap<>();
        query.fetchInto(record -> {
            Identifier<MAIN> key = RecordReader.createKey(record, aliasedKey);
            multiValuesMap.computeIfAbsent(key, k -> Lists.newArrayList());
            multiValuesMap.get(key).add(RecordReader.createFieldsValueMap(record, fields));
        });
        return multiValuesMap;
    }

    public <E extends EntityType<E>> TempTableResource<ImpersonatorTable> createForeignKeysTable(final DataTable primaryTable, final UniqueKey<E> foreignUniqueKey, final Collection<? extends Identifier<E>> keys) {
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

    private <E extends EntityType<E>> List<SelectField<?>> selectFieldsOf(Collection<? extends EntityField<?, ?>> fields, AliasedKey<E> aliasedKey) {
        return dbFieldsOf(fields).concat(aliasedKey.aliasedFields()).toList();
    }

    private Seq<SelectField<?>> dbFieldsOf(Collection<? extends EntityField<?, ?>> fields) {
        return seq(fields).flatMap(field -> field.getDbAdapter().getTableFields());
    }

    private <E extends EntityType<E>> E entityTypeOf(Collection<? extends EntityField<E, ?>> fields) {
        return (E) seq(fields).findFirst().get().getEntityType();
    }

    private Seq<EntityField<?, ?>> fieldsOf(final Collection<? extends Identifier<?>> ids) {
        return Seq.of(ids.iterator().next().getUniqueKey().getFields());
    }

    private <E extends EntityType<E>, PE extends PartialEntity> PE createInstance(final Class<PE> entityIface, Map<Method, EntityField<E, ?>> entityMethodsMap, CurrentEntityState currentState) {
        Class<?>[] interfaces = {entityIface};
        return (PE) Proxy.newProxyInstance(entityIface.getClassLoader(), interfaces, new PartialEntityInvocationHandler<>(entityMethodsMap, currentState));
    }
}