package com.kenshoo.pl.entity.internal.fetch;

import com.google.common.collect.Lists;
import com.kenshoo.jooq.QueryExtension;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.EntityImpl;
import org.jooq.Record;
import org.jooq.ResultQuery;
import org.jooq.SelectJoinStep;

import java.util.*;

import static org.jooq.lambda.Seq.seq;

public class NewEntityFetcher {

    private final QueryBuilder queryBuilder;

    public NewEntityFetcher(QueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
    }

    public <E extends EntityType<E>> Map<Identifier<E>, Entity> fetchEntitiesByIds(final Collection<? extends Identifier<E>> ids, final Collection<? extends EntityField<?, ?>> fieldsToFetch) {
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }

        final UniqueKey<E> uniqueKey = ids.iterator().next().getUniqueKey();
        final EntityType<E> primaryType = uniqueKey.getEntityType();
        final AliasedKey<E> aliasedKey = new AliasedKey<E>(uniqueKey);

        final FetchExecutionPlan.Result executionPlan = new FetchExecutionPlan().run(primaryType.getPrimaryTable(), fieldsToFetch);

        final QueryBuilder.Result mainQueryBuilder = queryBuilder.buildOneToOneQuery(executionPlan.getOneToOneGraph(), primaryType.getPrimaryTable(), aliasedKey, fieldsToFetch);
        final Map<Identifier<E>, Entity> entities = createEntities(mainQueryBuilder, primaryType, ids, aliasedKey);

        final List<QueryBuilder.Result> subQueriesBuilder = queryBuilder.buildManyToOneQueries(executionPlan.getManyToOneGraph(), primaryType.getPrimaryTable(), aliasedKey, fieldsToFetch);
        subQueriesBuilder.forEach(result -> {
            Map<Identifier<E>, List<FieldsValueMap<?>>> multiValuesMap = createMultiValues(result.getQuery(), primaryType, result.getFields(), ids, aliasedKey);
            multiValuesMap.forEach((id, multiValues) -> ((EntityImpl) entities.get(id)).add(entityTypeOf(result.getFields()), (List) multiValues));
        });

        return entities;
    }

    private <E extends EntityType<E>> Map<Identifier<E>, Entity> createEntities(QueryBuilder.Result queryBuilderResult, EntityType<E> EntityType, Collection<? extends Identifier<E>> ids, AliasedKey<E> aliasedKey) {
        final UniqueKey<E> uniqueKey = ids.iterator().next().getUniqueKey();
        try (QueryExtension<SelectJoinStep<Record>> queryExtender = queryBuilder.addIdsCondition(queryBuilderResult.getQuery(), EntityType.getPrimaryTable(), uniqueKey, ids)) {
            return fetchEntitiesMap(queryExtender.getQuery(), aliasedKey, queryBuilderResult.getFields());
        }
    }

    private <E extends EntityType<E>> Map<Identifier<E>, EntityImpl> fetchEntitiesMap(SelectJoinStep<Record> query, AliasedKey<E> aliasedKey, Collection<? extends EntityField<E, ?>> fields) {
        return query.fetchMap(record -> RecordReader.createKey(record, aliasedKey), record -> RecordReader.createEntity(record, fields));
    }

    private <E extends EntityType<E>> Map<Identifier<E>, List<FieldsValueMap<?>>> createMultiValues(SelectJoinStep<Record> primaryQuery, EntityType<E> primaryType, Collection<? extends EntityField<E, ?>> primaryFields, Collection<? extends Identifier<E>> ids, AliasedKey<E> aliasedKey) {
        final UniqueKey<E> uniqueKey = ids.iterator().next().getUniqueKey();
        try (QueryExtension<SelectJoinStep<Record>> queryExtender = queryBuilder.addIdsCondition(primaryQuery, primaryType.getPrimaryTable(), uniqueKey, ids)) {
            return fetchMultiValuesMap(queryExtender.getQuery(), aliasedKey, primaryFields);
        }
    }

    private <E extends EntityType<E>> Map<Identifier<E>, List<FieldsValueMap<?>>> fetchMultiValuesMap(ResultQuery<Record> query,AliasedKey<E> aliasedKey, Collection<? extends EntityField<E, ?>> fields) {
        final Map<Identifier<E>, List<FieldsValueMap<?>>> multiValuesMap = new HashMap<>();
        query.fetchInto(record -> {
            Identifier<E> key = RecordReader.createKey(record, aliasedKey);
            multiValuesMap.computeIfAbsent(key, k -> Lists.newArrayList());
            multiValuesMap.get(key).add(RecordReader.createFieldsValueMap(record, fields));
        });
        return multiValuesMap;
    }

    private <E extends EntityType<E>> E entityTypeOf(Collection<? extends EntityField<E, ?>> fields) {
        return (E) seq(fields).findFirst().get().getEntityType();
    }

}
