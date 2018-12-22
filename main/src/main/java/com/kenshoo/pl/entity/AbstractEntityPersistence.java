package com.kenshoo.pl.entity;

import com.kenshoo.pl.entity.internal.EntitiesFetcher;
import org.jooq.Condition;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

public abstract class AbstractEntityPersistence<E extends EntityType<E>, PK extends Identifier<E>> implements EntityPersistence<E, PK> {

    @Resource
    private CreatePersistenceLayer<E, PK> createPersistenceLayer;
    @Resource
    private UpdatePersistenceLayer<E> updatePersistenceLayer;
    @Resource
    private EntitiesFetcher entitiesFetcher;
    @Resource
    private DeletePersistenceLayer<E> deletePersistenceLayer;
    @Resource
    private InsertOnDuplicateUpdatePersistenceLayer<E> insertOnDuplicateUpdatePersistenceLayer;

    private final E entityType;
    private final UniqueKey<E> primaryKey;

    protected AbstractEntityPersistence(E entityType, UniqueKey<E> primaryKey) {
        this.entityType = entityType;
        this.primaryKey = primaryKey;
    }

    protected abstract ChangeFlowConfig.Builder<E> flowConfigBuilder();

    @Override
    public <ID extends Identifier<E>> UpdateEntityCommand<E, ID> buildUpdateCommand(ID id) {
        return new UpdateEntityCommand<>(entityType, id);
    }

    @Override
    public <ID extends Identifier<E>> DeleteEntityCommand<E, ID> buildDeleteCommand(ID id) {
        return new DeleteEntityCommand<>(entityType, id);
    }

    @Override
    public CreateResult<E, PK> create(List<? extends CreateEntityCommand<E>> commands) {
        return createPersistenceLayer.makeChanges(commands, flowConfigBuilder().build(), primaryKey);
    }

    @Override
    public CreateResult<E, PK> customCreate(List<? extends CreateEntityCommand<E>> commands, Function<ChangeFlowConfig.Builder<E>, ChangeFlowConfig.Builder<E>> flowConfigModifier) {
        return createPersistenceLayer.makeChanges(commands, flowConfigModifier.apply(flowConfigBuilder()).build(), primaryKey);
    }

    @Override
    public <ID extends Identifier<E>> UpdateResult<E, ID> update(List<? extends UpdateEntityCommand<E, ID>> commands) {
        return updatePersistenceLayer.makeChanges(commands, flowConfigBuilder().build());
    }

    @Override
    public <ID extends Identifier<E>> UpdateResult<E, ID>  customUpdate(List<? extends UpdateEntityCommand<E, ID>> commands, Function<ChangeFlowConfig.Builder<E>, ChangeFlowConfig.Builder<E>> flowConfigModifier) {
        return updatePersistenceLayer.makeChanges(commands, flowConfigModifier.apply(flowConfigBuilder()).build());
    }

    @Override
    public <ID extends Identifier<E>> DeleteResult<E, ID> delete(List<? extends DeleteEntityCommand<E, ID>> commands) {
        return deletePersistenceLayer.makeChanges(commands, flowConfigBuilder().build());
    }

    @Override
    public <ID extends Identifier<E>> DeleteResult<E, ID> customDelete(List<? extends DeleteEntityCommand<E, ID>> commands, Function<ChangeFlowConfig.Builder<E>, ChangeFlowConfig.Builder<E>> flowConfigModifier) {
        return deletePersistenceLayer.makeChanges(commands, flowConfigModifier.apply(flowConfigBuilder()).build());
    }

    @Override
    public <ID extends Identifier<E>> InsertOnDuplicateUpdateResult<E, ID> insertOnDuplicateUpdate(List<? extends InsertOnDuplicateUpdateCommand<E, ID>> commands) {
        return insertOnDuplicateUpdatePersistenceLayer.makeChanges(commands, flowConfigBuilder().build());
    }

    @Override
    public <ID extends Identifier<E>> InsertOnDuplicateUpdateResult<E, ID> customInsertOnDuplicateUpdate(List<? extends InsertOnDuplicateUpdateCommand<E, ID>> commands, Function<ChangeFlowConfig.Builder<E>, ChangeFlowConfig.Builder<E>> flowConfigModifier) {
        return insertOnDuplicateUpdatePersistenceLayer.makeChanges(commands, flowConfigModifier.apply(flowConfigBuilder()).build());
    }

    @Override
    public <UKV extends UniqueKeyValue<E>> Map<UKV, Entity> fetchEntities(Collection<UKV> keys, Collection<EntityField<?, ?>> fieldsToFetch) {
        if (keys.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Identifier<E>, Entity> entityMap = entitiesFetcher.fetchEntitiesByKeys(entityType, keys.iterator().next().getUniqueKey(), keys, fieldsToFetch);
        return keys.stream().filter(entityMap::containsKey).collect(toMap(identity(), entityMap::get));
    }

    @Override
    public <UKV extends UniqueKeyValue<E>, PE extends PartialEntity> Map<UKV, PE> fetchByKeys(Collection<UKV> keys, Class<PE> entityIface) {
        return entitiesFetcher.fetchPartialEntities(entityType, keys, entityIface);
    }

    @Override
    public <PE extends PartialEntity> List<PE> fetchByCondition(Condition condition, Class<PE> entityIface) {
        return entitiesFetcher.fetchByCondition(entityType, condition, entityIface);
    }
}
