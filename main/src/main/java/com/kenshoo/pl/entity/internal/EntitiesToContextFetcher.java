package com.kenshoo.pl.entity.internal;

import com.google.common.collect.Sets;
import com.kenshoo.pl.entity.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.kenshoo.pl.entity.ChangeOperation.CREATE;
import static com.kenshoo.pl.entity.UniqueKeyValue.concat;
import static com.kenshoo.pl.entity.internal.EntityCommandUtil.getAncestor;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;
import static org.jooq.lambda.Seq.seq;


public class EntitiesToContextFetcher {

    private final EntitiesFetcher entitiesFetcher;

    public EntitiesToContextFetcher(EntitiesFetcher entitiesFetcher) {
        this.entitiesFetcher = entitiesFetcher;
    }

    public <E extends EntityType<E>> void fetchEntities(Collection<? extends ChangeEntityCommand<E>> commands, ChangeOperation changeOperation, ChangeContext context, ChangeFlowConfig<E> flow) {

        commands.forEach(c -> context.addEntity(c, CurrentEntityState.EMPTY));

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
                .groupBy(FieldFetchRequest::getWhereToQuery)
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
        final CurrentEntityMutableState currentState = (CurrentEntityMutableState)context.getEntity(cmd);
        parentFields.forEach(field -> {
            Triptional triptional = getValue(context, ancestor, field);
            if(triptional.isPresent()) {
                currentState.set(field, triptional.get());
            }
        });
    }

    private Triptional<?> getValue(ChangeContext context, ChangeEntityCommand cmd, EntityField field) {
        return cmd.containsField(field) ? Triptional.of(cmd.get(field)) : context.getEntity(cmd).safeGet(field);
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

        Map<Identifier<E>, CurrentEntityState> fetchedEntities = entitiesFetcher.fetchEntitiesByIds(keysByCommand.values(), fieldsToFetch);
        addFetchedEntitiesToChangeContext(fetchedEntities, changeContext, keysByCommand);
    }

    private <E extends EntityType<E>> void fetchEntitiesByForeignKeys(Collection<? extends ChangeEntityCommand<E>> commands, Set<EntityField<?, ?>> fieldsToFetch, ChangeContext context, ChangeFlowConfig<E> flowConfig) {
        E entityType = flowConfig.getEntityType();
        Collection<EntityField<E, ?>> foreignKeys = entityType.determineForeignKeys(flowConfig.getRequiredRelationFields()).filter(not(new IsFieldReferringToParent<>(context.getHierarchy(), entityType))).collect(toList());
        if (foreignKeys.isEmpty()) {
            commands.forEach(cmd -> context.addEntity(cmd, new CurrentEntityMutableState()));
        } else {
            final UniqueKey<E> foreignUniqueKey = new ForeignUniqueKey<>(foreignKeys);
            Map<? extends ChangeEntityCommand<E>, Identifier<E>> keysByCommand = commands.stream().collect(toMap(identity(), foreignUniqueKey::createIdentifier));
            Map<Identifier<E>, CurrentEntityState> fetchedEntities = entitiesFetcher.fetchEntitiesByForeignKeys(entityType, foreignUniqueKey, Sets.newHashSet(keysByCommand.values()), fieldsToFetch);
            addFetchedEntitiesToChangeContext(fetchedEntities, context, keysByCommand);
        }
    }

    private <E extends EntityType<E>> void addFetchedEntitiesToChangeContext(Map<Identifier<E>, CurrentEntityState> fetchedEntities, ChangeContext changeContext, Map<? extends ChangeEntityCommand<E>, Identifier<E>> keysByCommand) {
        for (Map.Entry<? extends ChangeEntityCommand<E>, Identifier<E>> entry : keysByCommand.entrySet()) {
            ChangeEntityCommand<E> command = entry.getKey();
            Identifier<E> identifier = entry.getValue();
            CurrentEntityState currentState = fetchedEntities.get(identifier);
            if (currentState != null) {
                changeContext.addEntity(command, currentState);
            }
        }
    }

    private <E extends EntityType<E>> Predicate<ChangeEntityCommand<E>> notMissing(ChangeContext context) {
        return cmd -> context.getEntity(cmd) != CurrentEntityState.EMPTY;
    }
}
