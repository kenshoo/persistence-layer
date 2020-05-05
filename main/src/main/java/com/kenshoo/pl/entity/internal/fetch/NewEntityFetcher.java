package com.kenshoo.pl.entity.internal.fetch;

import com.google.common.collect.Lists;
import com.kenshoo.jooq.DataTable;
import com.kenshoo.jooq.QueryExtension;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.UniqueKey;
import com.kenshoo.pl.entity.internal.EntityImpl;
import org.jooq.*;
import org.jooq.lambda.Seq;

import java.util.*;

import static org.jooq.lambda.Seq.seq;

public class NewEntityFetcher implements Fetcher {

    private final QueryBuilder queryBuilder;

    public NewEntityFetcher(DSLContext dslContext) {
        this.queryBuilder = new QueryBuilder(dslContext);
    }

    @Override
    public <E extends EntityType<E>> Map<Identifier<E>, Entity> fetchEntitiesByIds(final Collection<? extends Identifier<E>> ids, final Collection<? extends EntityField<?, ?>> fieldsToFetch) {
        if (ids.isEmpty()) {
            return Collections.emptyMap();
        }

        final UniqueKey<E> uniqueKey = ids.iterator().next().getUniqueKey();
        final DataTable primaryTable = uniqueKey.getEntityType().getPrimaryTable();
        final AliasedKey<E> aliasedKey = new AliasedKey<E>(uniqueKey);

        final ExecutionPlan executionPlan = new ExecutionPlan(primaryTable, fieldsToFetch);

        final Map<Identifier<E>, Entity> entities;

        final ExecutionPlan.OneToOnePlan oneToOnePlan = executionPlan.getOneToOnePlan();
        final SelectJoinStep<Record> mainQuery = queryBuilder.buildOneToOneQuery(oneToOnePlan.getPaths(), selectFieldsOf(oneToOnePlan.getFields(), aliasedKey), oneToOnePlan.getSecondaryTableRelations(), primaryTable);
        try (QueryExtension<SelectJoinStep<Record>> queryExtender = queryBuilder.addIdsCondition(mainQuery, primaryTable, uniqueKey, ids)) {
            entities = fetchEntitiesMap(queryExtender.getQuery(), aliasedKey, oneToOnePlan.getFields());
        }

        executionPlan.getManyToOnePlans().forEach(plan -> {
            final SelectJoinStep<Record> subQuery = queryBuilder.buildManyToOneQuery(plan.getPath(), selectFieldsOf(plan.getFields(), aliasedKey), primaryTable);
            try (QueryExtension<SelectJoinStep<Record>> queryExtender = queryBuilder.addIdsCondition(subQuery, primaryTable, uniqueKey, ids)) {
                Map<Identifier<E>, List<FieldsValueMap<?>>> multiValuesMap = fetchMultiValuesMap(queryExtender.getQuery(), aliasedKey, plan.getFields());
                multiValuesMap.forEach((id, multiValues) -> ((EntityImpl) entities.get(id)).add(entityTypeOf(plan.getFields()), (List) multiValues));
            }
        });

        return entities;
    }

    @Override
    public List<Entity> fetch(EntityType<?> entityType, PLCondition plCondition, EntityField<?, ?>... fieldsToFetch) {
        throw new UnsupportedOperationException("NewEntityFetcher doesn't implement yet 'fetch' method...");
    }

    @Override
    public <E extends EntityType<E>> Map<Identifier<E>, Entity> fetchEntitiesByForeignKeys(E entityType, UniqueKey<E> foreignUniqueKey, Collection<? extends Identifier<E>> keys, Collection<EntityField<?, ?>> fieldsToFetch) {
        throw new UnsupportedOperationException("NewEntityFetcher doesn't implement yet 'fetchEntitiesByForeignKeys' method...");
    }

    @Override
    public <E extends EntityType<E>, PE extends PartialEntity, ID extends Identifier<E>> Map<ID, PE> fetchPartialEntities(E entityType, Collection<ID> keys, Class<PE> entityIface) {
        throw new UnsupportedOperationException("NewEntityFetcher doesn't implement yet 'fetchPartialEntities' method...");
    }

    @Override
    public <E extends EntityType<E>, PE extends PartialEntity> List<PE> fetchByCondition(E entityType, Condition condition, Class<PE> entityIface) {
        throw new UnsupportedOperationException("NewEntityFetcher doesn't implement yet 'fetchByCondition' method...");
    }

    private <E extends EntityType<E>> List<SelectField<?>> selectFieldsOf(List<? extends EntityField<?, ?>> fields, AliasedKey<E> aliasedKey) {
        return dbFieldsOf(fields).concat(aliasedKey.aliasedFields()).toList();
    }

    private Seq<SelectField<?>> dbFieldsOf(Collection<? extends EntityField<?, ?>> fields) {
        return seq(fields).flatMap(field -> field.getDbAdapter().getTableFields());
    }


    private <E extends EntityType<E>> Map<Identifier<E>, Entity> fetchEntitiesMap(SelectJoinStep<Record> query, AliasedKey<E> aliasedKey, List<? extends EntityField<?, ?>> fields) {
        return query.fetchMap(record -> RecordReader.createKey(record, aliasedKey), record -> RecordReader.createEntity(record, fields));
    }

    private <MAIN extends EntityType<MAIN>, SUB extends EntityType<SUB>> Map<Identifier<MAIN>, List<FieldsValueMap<?>>> fetchMultiValuesMap(ResultQuery<Record> query, AliasedKey<MAIN> aliasedKey, List<? extends EntityField<SUB, ?>> fields) {
        final Map<Identifier<MAIN>, List<FieldsValueMap<?>>> multiValuesMap = new HashMap<>();
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
