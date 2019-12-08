package com.kenshoo.pl.entity;

import com.kenshoo.pl.entity.internal.MissingChildrenSupplier;
import com.kenshoo.pl.entity.internal.ChildrenIdFetcher;
import org.jooq.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static org.jooq.lambda.Seq.seq;

public class MissingChildrenHandler<PARENT extends EntityType<PARENT>, CHILD extends EntityType<CHILD>> {

    private final ChildrenIdFetcher childrenIdFetcher;

    public MissingChildrenHandler(DSLContext jooq) {
        childrenIdFetcher = new ChildrenIdFetcher(jooq);
    }

    public void
    handle(Collection<? extends ChangeEntityCommand<PARENT>> parents, ChangeFlowConfig<PARENT> config) {
        seq(config.childFlows())
                .forEach(childflow -> {
                    final CHILD childType = (CHILD) childflow.getEntityType();
                    final Collection<? extends ChangeEntityCommand<PARENT>> parentsWithChildSupplier = seq(parents).filter(parent -> havingSupplierFor(parent, childType)).toList();
                    if (!parentsWithChildSupplier.isEmpty()) {
                        handleFor(parentsWithChildSupplier, childType);
                    }
                });
    }

    private void
    handleFor(Collection<? extends ChangeEntityCommand<PARENT>> parents, CHILD childType) {
        final EntityType.ForeignKey<CHILD, PARENT> keyToParent = childType.getKeyTo(first(parents).getEntityType());
        final Map<Identifier<CHILD>, Set<Identifier<CHILD>>> missingChildrenByParents = getMissingChildByParents(parents, childType);
        seq(parents).forEach(parent -> {
            final MissingChildrenSupplier<CHILD> missingChildrenSupplier = parent.getMissingChildrenSupplier(childType).get();
            final Set<Identifier<CHILD>> missingChildIds = missingChildrenByParents.getOrDefault(getChildParentId(parent, keyToParent), emptySet());
            seq(missingChildIds).forEach(childId -> {
                missingChildrenSupplier.supplyNewCommand(childId).ifPresent(parent::addChild);
            });
        });
    }

    private Map<Identifier<CHILD>, Set<Identifier<CHILD>>>
    getMissingChildByParents(Collection<? extends ChangeEntityCommand<PARENT>> parentsCmd, CHILD childType) {
        try (Stream<FullIdentifier<CHILD>> childIdsFromDB = childrenIdFetcher.fetch(parentsCmd, childType)) {
            Set<FullIdentifier<CHILD>> childIdsFromCmd = collectChildIds(parentsCmd, childType);
            Stream<FullIdentifier<CHILD>> missingChildIds = childIdsFromDB.filter(notIn(childIdsFromCmd));
            return groupByParents(missingChildIds);
        }
    }

    private Map<Identifier<CHILD>, Set<Identifier<CHILD>>>
    groupByParents(Stream<FullIdentifier<CHILD>> childIds) {
        return childIds
                .collect(Collectors.groupingBy(
                        FullIdentifier::getParentId,
                        Collectors.mapping(FullIdentifier::getChildId, Collectors.toSet())));
    }

    private Set<FullIdentifier<CHILD>>
    collectChildIds(Collection<? extends ChangeEntityCommand<PARENT>> parents, CHILD childType) {
        final EntityType.ForeignKey<CHILD, PARENT> keyToParent = childType.getKeyTo(first(parents).getEntityType());
        return seq(parents)
                .flatMap(parent -> parent.getChildren(childType))
                .filter(child -> child.getIdentifier() != null)
                .map(childCmd -> getChildFullId(keyToParent, childCmd))
                .toSet();
    }

    private FullIdentifier<CHILD> getChildFullId(EntityType.ForeignKey<CHILD, PARENT> keyToParent, ChangeEntityCommand<CHILD> childCmd) {
        final Identifier<CHILD> parentId = getChildParentId(childCmd.getParent(), keyToParent);
        final Identifier<CHILD> childId = childCmd.getIdentifier();
        return new FullIdentifier<>(parentId, childId);
    }

    private Identifier<CHILD>
    getChildParentId(ChangeEntityCommand<PARENT> parentCmd, EntityType.ForeignKey<CHILD, PARENT> keyToParent) {
        Object[] values = keyToParent.to().stream().map(field-> parentCmd.getIdentifier().get(field)).toArray();
        return new UniqueKeyValue<>(new UniqueKey<>(keyToParent.from()), values);
    }

    private boolean havingSupplierFor(ChangeEntityCommand<PARENT> parent, CHILD childType) {
        return parent.getMissingChildrenSupplier(childType).isPresent();
    }

    private <T> T first(Collection<T> collection) {
        return collection.iterator().next();
    }

    private <T> Predicate<T> notIn(Set<T> set) {
        return item -> !set.contains(item);
    }
}
