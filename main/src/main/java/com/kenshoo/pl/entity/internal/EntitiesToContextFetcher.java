package com.kenshoo.pl.entity.internal;

import com.google.common.collect.Sets;
import com.kenshoo.pl.entity.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static com.kenshoo.pl.entity.UniqueKeyValue.concat;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;


@Component
public class EntitiesToContextFetcher {

    @Resource
    private EntitiesFetcher entitiesFetcher;


    public <E extends EntityType<E>> void fetchEntitiesByKeys(Collection<? extends ChangeEntityCommand<E>> commands, Set<EntityField<?, ?>> fieldsToFetch, ChangeContext changeContext, ChangeFlowConfig<E> flowConfig) {
        Map<? extends ChangeEntityCommand<E>, Identifier<E>> keysByCommand = commands.stream().collect(toMap(
                Function.identity(),
                cmd -> concat(cmd.getIdentifier(), cmd.getKeysToParent())));
        //noinspection ConstantConditions
        UniqueKey<E> uniqueKey = keysByCommand.values().iterator().next().getUniqueKey();
        Map<Identifier<E>, Entity> fetchedEntities = entitiesFetcher.fetchEntitiesByKeys(flowConfig.getEntityType(), uniqueKey, keysByCommand.values(), fieldsToFetch);
        addFetchedEntitiesToChangeContext(fetchedEntities, changeContext, keysByCommand);
    }

    public <E extends EntityType<E>> void fetchEntitiesByForeignKeys(Collection<? extends ChangeEntityCommand<E>> commands, Set<EntityField<?, ?>> fieldsToFetch, ChangeContext changeContext, ChangeFlowConfig<E> flowConfig) {
        E entityType = flowConfig.getEntityType();
        Collection<EntityField<E, ?>> foreignKeys = entityType.determineForeignKeys(flowConfig.getRequiredRelationFields());
        if (foreignKeys.isEmpty()) {
            return;
        }
        final UniqueKey<E> foreignUniqueKey = new ForeignUniqueKey<>(foreignKeys);
        Map<? extends ChangeEntityCommand<E>, Identifier<E>> keysByCommand = commands.stream().collect(toMap(identity(), foreignUniqueKey::createValue));
        Map<Identifier<E>, Entity> fetchedEntities = entitiesFetcher.fetchEntitiesByForeignKeys(entityType, foreignUniqueKey, Sets.newHashSet(keysByCommand.values()), fieldsToFetch);
        addFetchedEntitiesToChangeContext(fetchedEntities, changeContext, keysByCommand);
    }


    private <E extends EntityType<E>> void addFetchedEntitiesToChangeContext(Map<Identifier<E>, Entity> fetchedEntities, ChangeContext changeContext, Map<? extends ChangeEntityCommand<E>, Identifier<E>> keysByCommand) {
        for (Map.Entry<? extends ChangeEntityCommand<E>, Identifier<E>> entry : keysByCommand.entrySet()) {
            ChangeEntityCommand<E> command = entry.getKey();
            Identifier<E> identifier = entry.getValue();
            Entity entity = fetchedEntities.get(identifier);
            if (entity != null) {
                changeContext.addEntity(command, entity);
            }
        }
    }

}
