package com.kenshoo.pl.entity;

import org.jooq.Condition;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface EntityPersistence<E extends EntityType<E>, PK extends Identifier<E>> {

    CreateEntityCommand<E> buildCreateCommand();

    CreateResult<E, PK> create(List<? extends CreateEntityCommand<E>> commands);

    CreateResult<E, PK> customCreate(List<? extends CreateEntityCommand<E>> commands, Function<ChangeFlowConfig.Builder<E>, ChangeFlowConfig.Builder<E>> flowConfigModifier);


    <ID extends Identifier<E>> UpdateEntityCommand<E, ID> buildUpdateCommand(ID id);

    <ID extends Identifier<E>> UpdateResult<E, ID> update(List<? extends UpdateEntityCommand<E, ID>> commands);

    <ID extends Identifier<E>> UpdateResult<E, ID> customUpdate(List<? extends UpdateEntityCommand<E, ID>> commands, Function<ChangeFlowConfig.Builder<E>, ChangeFlowConfig.Builder<E>> flowConfigModifier);


    <ID extends Identifier<E>> DeleteEntityCommand<E, ID> buildDeleteCommand(ID id);

    <ID extends Identifier<E>> DeleteResult<E, ID> delete(List<? extends DeleteEntityCommand<E, ID>> commands);

    <ID extends Identifier<E>> DeleteResult<E, ID> customDelete(List<? extends DeleteEntityCommand<E, ID>> commands, Function<ChangeFlowConfig.Builder<E>, ChangeFlowConfig.Builder<E>> flowConfigModifier);

    <ID extends Identifier<E>> InsertOnDuplicateUpdateResult<E, ID> insertOnDuplicateUpdate(List<? extends InsertOnDuplicateUpdateCommand<E, ID>> commands);

    <ID extends Identifier<E>> InsertOnDuplicateUpdateResult<E, ID> customInsertOnDuplicateUpdate(List<? extends InsertOnDuplicateUpdateCommand<E, ID>> commands, Function<ChangeFlowConfig.Builder<E>, ChangeFlowConfig.Builder<E>> flowConfigModifier);

    <UKV extends UniqueKeyValue<E>> Map<UKV, CurrentEntityState> fetchEntities(Collection<UKV> keys, Collection<EntityField<?, ?>> fieldsToFetch);

    <UKV extends UniqueKeyValue<E>, PE extends PartialEntity> Map<UKV, PE> fetchByKeys(Collection<UKV> keys, final Class<PE> entityIface);

    <PE extends PartialEntity> List<PE> fetchByCondition(Condition condition, final Class<PE> entityIface);
}