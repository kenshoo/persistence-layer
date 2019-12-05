package com.kenshoo.pl.entity.internal;

import com.google.common.collect.Sets;
import com.kenshoo.pl.entity.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.kenshoo.pl.entity.ChangeOperation.CREATE;
import static com.kenshoo.pl.entity.UniqueKeyValue.concat;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.jooq.lambda.Seq.seq;


public class EntitiesToContextFetcher {

    private final EntitiesFetcher entitiesFetcher;

    public EntitiesToContextFetcher(EntitiesFetcher entitiesFetcher) {
        this.entitiesFetcher = entitiesFetcher;
    }

    public <E extends EntityType<E>> void fetchEntities(Collection<? extends ChangeEntityCommand<E>> commands, ChangeOperation changeOperation, ChangeContext context, ChangeFlowConfig<E> flow) {

        commands.forEach(c -> context.addEntity(c, Entity.EMPTY));

        final Set<EntityField<?, ?>> fieldsToFetch = context.getFetchRequests().stream().
                filter( r -> r.getWhereToQuery().equals(flow.getEntityType()) && r.supports(changeOperation)).
                map(FieldFetchRequest::getEntityField).
                collect(toSet());

        if (changeOperation == CREATE) {
            fetchEntitiesByForeignKeys(commands, fieldsToFetch, context, flow);
        } else {
            fetchEntitiesByKeys(commands, fieldsToFetch, context, flow);
        }

        populateFieldsFromAllParents(seq(commands).filter(notMissing(context)).toList(), context, flow.getEntityType());
    }

    private <E extends EntityType<E>> void populateFieldsFromAllParents(List<? extends ChangeEntityCommand<E>> commands, ChangeContext context, EntityType<E> currentLevel) {

        if (commands.isEmpty()) {
            return;
        }

        seq(context.getFetchRequests())
                .filter(askedBy(currentLevel).and(not(queriedOn(currentLevel))))
                .groupBy(r -> r.getWhereToQuery())
                .forEach((level, requestedFields) -> {
                    final List<EntityField> parentFields = seq(requestedFields).map(f -> (EntityField)f.getEntityField()).toList();
                    commands.forEach(cmd -> populateFieldsFromOneLevel(context, level, parentFields, cmd));
                });
    }

    private <E extends EntityType<E>> void populateFieldsFromOneLevel(
            ChangeContext context,
            EntityType parentLevel,
            List<EntityField> parentFields,
            ChangeEntityCommand<E> cmd) {

        final ChangeEntityCommand ancestor = getAncestor(cmd, parentLevel);
        final EntityImpl entity = (EntityImpl)context.getEntity(cmd);
        seq(parentFields).forEach(field -> entity.set(field, getValue(context, ancestor, field)));
    }


    ChangeEntityCommand getAncestor(ChangeEntityCommand cmd, EntityType level) {
        for (ChangeEntityCommand parent = cmd.getParent(); parent != null; parent = parent.getParent()) {
            if (parent.getEntityType().equals(level)) {
                return parent;
            }
        }
        throw new RuntimeException("didn't find ancestor of level " + level.getName() + " for command with entity " + cmd.getEntityType().getName());
    }

    private Object getValue(ChangeContext context, ChangeEntityCommand cmd, EntityField field) {
        return cmd.containsField(field) ? cmd.get(field) : context.getEntity(cmd).get(field);
    }

    private Predicate<FieldFetchRequest> askedBy(EntityType e) {
        return r -> r.getWhoAskedForThis().equals(e);
    }

    private <T> Predicate<T> not(Predicate<T> p) {
        return p.negate();
    }

    private Predicate<FieldFetchRequest> queriedOn(EntityType e) {
        return r -> r.getWhereToQuery().equals(e);
    }

    private <E extends EntityType<E>> void fetchEntitiesByKeys(Collection<? extends ChangeEntityCommand<E>> commands, Set<EntityField<?, ?>> fieldsToFetch, ChangeContext changeContext, ChangeFlowConfig<E> flowConfig) {
        Map<? extends ChangeEntityCommand<E>, Identifier<E>> keysByCommand = commands.stream().collect(toMap(
                Function.identity(),
                cmd -> concat(cmd.getIdentifier(), cmd.getKeysToParent())));
        //noinspection ConstantConditions
        UniqueKey<E> uniqueKey = keysByCommand.values().iterator().next().getUniqueKey();
        Map<Identifier<E>, Entity> fetchedEntities = entitiesFetcher.fetchEntitiesByKeys(flowConfig.getEntityType(), uniqueKey, keysByCommand.values(), fieldsToFetch);
        addFetchedEntitiesToChangeContext(fetchedEntities, changeContext, keysByCommand);
    }

    private <E extends EntityType<E>> void fetchEntitiesByForeignKeys(Collection<? extends ChangeEntityCommand<E>> commands, Set<EntityField<?, ?>> fieldsToFetch, ChangeContext changeContext, ChangeFlowConfig<E> flowConfig) {
        E entityType = flowConfig.getEntityType();
        Collection<EntityField<E, ?>> foreignKeys = entityType.determineForeignKeys(flowConfig.getRequiredRelationFields());
        if (foreignKeys.isEmpty()) {
            commands.forEach(cmd -> changeContext.addEntity(cmd, new EntityImpl()));
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

    private <E extends EntityType<E>> Predicate<ChangeEntityCommand<E>> notMissing(ChangeContext context) {
        return cmd -> context.getEntity(cmd) != Entity.EMPTY;
    }
}
