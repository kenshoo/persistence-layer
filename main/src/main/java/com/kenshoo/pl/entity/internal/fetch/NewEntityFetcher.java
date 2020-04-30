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

        final ExecutionPlan executionPlan = new ExecutionPlan(primaryType.getPrimaryTable(), fieldsToFetch);

        final Map<Identifier<E>, Entity> entities;
        final QueryBuilder.Result mainQueryBuilder = queryBuilder.buildOneToOneQuery(executionPlan.getOneToOnePaths(), primaryType.getPrimaryTable(), aliasedKey, fieldsToFetch);
        try (QueryExtension<SelectJoinStep<Record>> queryExtender = queryBuilder.addIdsCondition(mainQueryBuilder.getQuery(), primaryType.getPrimaryTable(), uniqueKey, ids)) {
            entities =  fetchEntitiesMap(queryExtender.getQuery(), aliasedKey, mainQueryBuilder.getFields());
        }

        final List<QueryBuilder.Result> subQueriesBuilder = queryBuilder.buildManyToOneQueries(executionPlan.getManyToOnePaths(), primaryType.getPrimaryTable(), aliasedKey, fieldsToFetch);
        subQueriesBuilder.forEach(result -> {
            try (QueryExtension<SelectJoinStep<Record>> queryExtender = queryBuilder.addIdsCondition(result.getQuery(), primaryType.getPrimaryTable(), uniqueKey, ids)) {
                Map<Identifier<E>, List<FieldsValueMap<?>>> multiValuesMap =  fetchMultiValuesMap(queryExtender.getQuery(), aliasedKey, result.getFields());
                multiValuesMap.forEach((id, multiValues) -> ((EntityImpl) entities.get(id)).add(entityTypeOf(result.getFields()), (List) multiValues));
            }
        });

        return entities;
    }

    private <E extends EntityType<E>> Map<Identifier<E>, EntityImpl> fetchEntitiesMap(SelectJoinStep<Record> query, AliasedKey<E> aliasedKey, Collection<? extends EntityField<E, ?>> fields) {
        return query.fetchMap(record -> RecordReader.createKey(record, aliasedKey), record -> RecordReader.createEntity(record, fields));
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
