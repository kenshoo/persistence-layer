package com.kenshoo.pl.entity.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.kenshoo.jooq.DataTable;
import com.kenshoo.jooq.QueryExtension;
import com.kenshoo.pl.entity.UniqueKey;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.fetch.*;
import org.jooq.*;
import org.jooq.lambda.Seq;
import java.util.*;

import static com.kenshoo.pl.entity.Feature.FetchMany;
import static org.jooq.lambda.Seq.seq;


public class EntitiesFetcher {

    private final DSLContext dslContext;
    private final FeatureSet features;
    private final OldEntityFetcher oldEntityFetcher;

    public EntitiesFetcher(DSLContext dslContext) {
        this(dslContext, FeatureSet.EMPTY);
    }

    public EntitiesFetcher(DSLContext dslContext, FeatureSet features) {
        this.dslContext = dslContext;
        this.features = features;
        this.oldEntityFetcher = new OldEntityFetcher(dslContext);
    }

    public <E extends EntityType<E>> Map<Identifier<E>, Entity> fetchEntitiesByKeys(final E entityType,
                                                                                    final UniqueKey<E> uniqueKey,
                                                                                    final Collection<? extends Identifier<E>> keys,
                                                                                    final Collection<? extends EntityField<?, ?>> fieldsToFetch) {
        return oldEntityFetcher.fetchEntitiesByIds(keys, fieldsToFetch);

    }

    public <E extends EntityType<E>> Map<Identifier<E>, Entity> fetchEntitiesByIds(final Collection<? extends Identifier<E>> ids,
                                                                                   final EntityField<?, ?>... fieldsToFetchArgs) {
        return fetchEntitiesByIds(ids, ImmutableList.copyOf(fieldsToFetchArgs));
    }

    public <E extends EntityType<E>> Map<Identifier<E>, Entity> fetchEntitiesByIds(final Collection<? extends Identifier<E>> ids,
                                                                                   final Collection<? extends EntityField<?, ?>> fieldsToFetch) {
        if (!features.isEnabled(FetchMany)) {
            return oldEntityFetcher.fetchEntitiesByIds(ids, fieldsToFetch);
        }

        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }

        final UniqueKey<E> uniqueKey = ids.iterator().next().getUniqueKey();
        final DataTable primaryTable = uniqueKey.getEntityType().getPrimaryTable();
        final AliasedKey<E> aliasedKey = new AliasedKey<>(uniqueKey);

        final ExecutionPlan executionPlan = new ExecutionPlan(primaryTable, fieldsToFetch);

        final Map<Identifier<E>, Entity> mainEntities;

        final ExecutionPlan.OneToOnePlan oneToOnePlan = executionPlan.getOneToOnePlan();

        try (QueryExtension<SelectJoinStep<Record>> queryExtender = new QueryBuilder<E>(dslContext).selecting(selectFieldsOf(oneToOnePlan.getFields(), aliasedKey))
                .from(primaryTable)
                .innerJoin(oneToOnePlan.getPaths())
                .leftJoin(oneToOnePlan.getSecondaryTableRelations())
                .whereIdsIn(ids)
                .build()) {
            mainEntities = fetchEntitiesMap(queryExtender.getQuery(), aliasedKey, oneToOnePlan.getFields());
        }

        executionPlan.getManyToOnePlans().forEach(plan -> fetchAndPopulateSubEntities(ids, primaryTable, aliasedKey, mainEntities, plan));

        return mainEntities;
    }

    private <E extends EntityType<E>, SUB extends EntityType<SUB>> void fetchAndPopulateSubEntities(
            Collection<? extends Identifier<E>> ids,
            DataTable primaryTable,
            AliasedKey<E> aliasedKey,
            Map<Identifier<E>, Entity> entities,
            ExecutionPlan.ManyToOnePlan<SUB> plan) {

        try (QueryExtension<SelectJoinStep<Record>> queryExtender = new QueryBuilder<E>(dslContext).selecting(selectFieldsOf(plan.getFields(), aliasedKey))
                .from(primaryTable)
                .innerJoin(plan.getPath())
                .whereIdsIn(ids)
                .build()) {
            Map<Identifier<E>, List<FieldsValueMap<SUB>>> multiValuesMap = fetchMultiValuesMap(queryExtender.getQuery(), aliasedKey, plan.getFields());
            multiValuesMap.forEach((Identifier<E> id, List<FieldsValueMap<SUB>> multiValues) -> {
                final SUB subEntityType = entityTypeOf(plan.getFields());
                ((EntityImpl) entities.get(id)).add(subEntityType, multiValues);
            });
        }
    }

    public List<Entity> fetch(final EntityType<?> entityType,
                              final PLCondition plCondition,
                              final EntityField<?, ?>... fieldsToFetch) {
        return oldEntityFetcher.fetch(entityType, plCondition, fieldsToFetch);
    }

    public <E extends EntityType<E>> Map<Identifier<E>, Entity> fetchEntitiesByForeignKeys(E entityType, UniqueKey<E> foreignUniqueKey, Collection<? extends Identifier<E>> keys, Collection<EntityField<?, ?>> fieldsToFetch) {
        return oldEntityFetcher.fetchEntitiesByForeignKeys(entityType, foreignUniqueKey, keys, fieldsToFetch);
    }

    public <E extends EntityType<E>, PE extends PartialEntity, ID extends Identifier<E>> Map<ID, PE> fetchPartialEntities(E entityType, Collection<ID> keys, final Class<PE> entityIface) {
        return oldEntityFetcher.fetchPartialEntities(entityType, keys, entityIface);
    }

    public <E extends EntityType<E>, PE extends PartialEntity> List<PE> fetchByCondition(E entityType, Condition condition, final Class<PE> entityIface) {
        return oldEntityFetcher.fetchByCondition(entityType, condition, entityIface);
    }

    private <E extends EntityType<E>> List<SelectField<?>> selectFieldsOf(Collection<? extends EntityField<?, ?>> fields, AliasedKey<E> aliasedKey) {
        return dbFieldsOf(fields).concat(aliasedKey.aliasedFields()).toList();
    }

    private Seq<SelectField<?>> dbFieldsOf(Collection<? extends EntityField<?, ?>> fields) {
        return seq(fields).flatMap(field -> field.getDbAdapter().getTableFields());
    }


    private <E extends EntityType<E>> Map<Identifier<E>, Entity> fetchEntitiesMap(ResultQuery<Record> query, AliasedKey<E> aliasedKey, List<? extends EntityField<?, ?>> fields) {
        return query.fetchMap(record -> RecordReader.createKey(record, aliasedKey), record -> RecordReader.createEntity(record, fields));
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

    private <E extends EntityType<E>> E entityTypeOf(Collection<? extends EntityField<E, ?>> fields) {
        return (E) seq(fields).findFirst().get().getEntityType();
    }
}
