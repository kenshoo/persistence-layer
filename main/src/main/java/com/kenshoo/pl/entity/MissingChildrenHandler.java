package com.kenshoo.pl.entity;

import com.google.common.annotations.VisibleForTesting;
import com.kenshoo.pl.entity.internal.ChildrenIdFetcher;
import org.jooq.DSLContext;

import java.util.*;
import java.util.stream.Stream;

import static com.kenshoo.pl.entity.UniqueKeyValue.concat;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.*;
import static org.jooq.lambda.Seq.seq;

public class MissingChildrenHandler {

    private final ChildrenIdFetcher childrenIdFetcher;

    public MissingChildrenHandler(DSLContext jooq) {
        childrenIdFetcher = new ChildrenIdFetcher(jooq);
    }

    @VisibleForTesting
    public MissingChildrenHandler(ChildrenIdFetcher childrenIdFetcher) {
        this.childrenIdFetcher = childrenIdFetcher;
    }

    public <PARENT extends EntityType<PARENT>>
    void handleRecursive(Iterable<? extends ChangeEntityCommand<PARENT>> parents, ChangeFlowConfig<PARENT> config) {

        final Collection<? extends ChangeEntityCommand<PARENT>> parentsWithChildSupplier = seq(parents)
                .filter(this::recursivelyCheckIfAnyShouldCascade)
                .toList();

        if (parentsWithChildSupplier.isEmpty()) {
            return;
        }

        seq(config.childFlows()).forEach(childflow -> handleChildFlow(parentsWithChildSupplier, childflow));
    }

    private <PARENT extends EntityType<PARENT>, CHILD extends EntityType<CHILD>>
    void handleChildFlow(Collection<? extends ChangeEntityCommand<PARENT>> parents, ChangeFlowConfig<CHILD> childFlow) {
        final CHILD childType = childFlow.getEntityType();
        ChildrenFromDB<PARENT, CHILD> childrenFromDB = getExistingChildrenFromDB(parents, childType);
        populateKeyToParent(parents, childType, childrenFromDB);
        populateCommandForMissingChildren(parents, childType, childrenFromDB);
        populateDeletionCommandForCascadeChildren(parents, childType, childrenFromDB);
        handleRecursive(seq(parents).flatMap(p -> p.getChildren(childType)), childFlow);
    }

    private <PARENT extends EntityType<PARENT>, CHILD extends EntityType<CHILD>>
    ChildrenFromDB<PARENT, CHILD> getExistingChildrenFromDB(Collection<? extends ChangeEntityCommand<PARENT>> parents, CHILD childType) {
        List<Identifier<PARENT>> parentIds = seq(parents).map(p -> concatenatedId(p)).toList();
        final UniqueKey<CHILD> childKey = identifierOfFirstChildCmd(parents, childType).orElseGet(childType::getPrimaryKey);
        return new ChildrenFromDB<>(childrenIdFetcher.fetch(parentIds, childKey));
    }

    private <PARENT extends EntityType<PARENT>, CHILD extends EntityType<CHILD>>
    void populateCommandForMissingChildren(Collection<? extends ChangeEntityCommand<PARENT>> parents, CHILD childType, ChildrenFromDB<PARENT, CHILD> childrenFromDB) {
        seq(parents).forEach(parent -> {
            final Set<Identifier<CHILD>> childrenFromCommand = childrenIdsOf(childType, parent);
            ChildrenWithKeyToParent<CHILD> parentChildrenFromDB = childrenFromDB.of(parent);
            seq(parentChildrenFromDB.map)
                    .filter(childIds -> !childrenFromCommand.contains(childIds.v1))
                    .forEach(missingChildIds -> {
                        parent.getMissingChildrenSupplier(childType).flatMap(s -> s.supplyNewCommand(missingChildIds.v1)).ifPresent(newCmd -> {
                            parent.addChild(newCmd);
                            newCmd.setKeysToParent(missingChildIds.v2);
                        });
                    });
        });
    }

    private <PARENT extends EntityType<PARENT>, CHILD extends EntityType<CHILD>>
    void populateDeletionCommandForCascadeChildren(Collection<? extends ChangeEntityCommand<PARENT>> parents, CHILD childType, ChildrenFromDB<PARENT, CHILD> childrenFromDB) {
        seq(parents)
                .filter(this::isCascadeDeletion)
                .forEach(parent -> {
                    ChildrenWithKeyToParent<CHILD> parentChildrenFromDB = childrenFromDB.of(parent);
                    seq(parentChildrenFromDB.map)
                            .forEach(childId -> {
                                Identifier<CHILD> key = childId.v1;
                                DeleteEntityCommand<CHILD, ? extends Identifier<CHILD>> cmd = new DeleteEntityCommand<>(childType, key).setCascade();
                                parent.addChild(cmd);
                                cmd.setKeysToParent(childId.v2);
                            });
                });
    }

    private <PARENT extends EntityType<PARENT>, CHILD extends EntityType<CHILD>>
    void populateKeyToParent(Collection<? extends ChangeEntityCommand<PARENT>> parents, CHILD childType, ChildrenFromDB<PARENT, CHILD> childrenFromDB) {
        seq(parents).forEach(parent -> {
            ChildrenWithKeyToParent<CHILD> childrenOfParent = childrenFromDB.of(parent);
            parent.getChildren(childType).forEach(child -> child.setKeysToParent(childrenOfParent.keyToParentOf(child.getIdentifier())));
        });
    }

    private static <E extends EntityType<E>> Identifier<E> concatenatedId(ChangeEntityCommand<E> entity) {
        return concat(entity.getIdentifier(), entity.getKeysToParent());
    }

    private <PARENT extends EntityType<PARENT>, CHILD extends EntityType<CHILD>>
    Set<Identifier<CHILD>> childrenIdsOf(CHILD childType, ChangeEntityCommand<PARENT> parent) {
        return parent.getChildren(childType).map(EntityChange::getIdentifier)
                .filter(Objects::nonNull)
                .collect(toSet());
    }

    private <E extends EntityType<E>>
    boolean recursivelyCheckIfAnyShouldCascade(ChangeEntityCommand<E> parent) {
        return !parent.getMissingChildrenSuppliers().isEmpty() || isCascadeDeletion(parent) || parent.getChildren().anyMatch(child -> recursivelyCheckIfAnyShouldCascade(child));
    }

    private <E extends EntityType<E>>
    boolean isCascadeDeletion(ChangeEntityCommand<E> cmd) {
        return cmd instanceof DeleteEntityCommand && ((DeleteEntityCommand) cmd).isCascade();
    }

    private
    <PARENT extends EntityType<PARENT>, CHILD extends EntityType<CHILD>>
    Optional<UniqueKey<CHILD>>
    identifierOfFirstChildCmd(Collection<? extends ChangeEntityCommand<PARENT>> parents, CHILD childType) {
        return parents.stream()
                .flatMap(p -> p.getChildren(childType))
                .map(EntityChange::getIdentifier)
                .filter(Objects::nonNull)
                .map(Identifier::getUniqueKey)
                .findFirst();
    }

    /**
     * Lookup maps for everything we fetched from DB.
     * Parent identifiers are concatenated IDs (see method concatenatedId) so they are always unique.
     */
    private static class ChildrenFromDB<PARENT extends EntityType<PARENT>, CHILD extends EntityType<CHILD>> {
        final Map<Identifier<PARENT>, ChildrenWithKeyToParent<CHILD>> map;
        final ChildrenWithKeyToParent<CHILD> EMPTY = new ChildrenWithKeyToParent<>(emptyMap());

        public ChildrenFromDB(Stream<FullIdentifier<PARENT, CHILD>> stream) {
            map = stream.collect(groupingBy(FullIdentifier::getParentId,
                    collectingAndThen(toMap(FullIdentifier::getChildId, FullIdentifier::getKetToParent), ChildrenWithKeyToParent::new)));
            stream.close();
        }

        ChildrenWithKeyToParent<CHILD> of(ChangeEntityCommand<PARENT> parent) {
            return map.getOrDefault(concatenatedId(parent), EMPTY);
        }
    }

    /**
     * This is a subset of what we fetched from DB: these are the children of
     * a specific parent.
     */
    private static class ChildrenWithKeyToParent<CHILD extends EntityType<CHILD>> {
        // child identifier (from the original command) mapped to the child keyToParent.
        // this is required to populate keyToParent for the child commands to be available
        // for the next recursive iteration.
        final Map<Identifier<CHILD>, Identifier<CHILD>> map;

        private ChildrenWithKeyToParent(Map<Identifier<CHILD>, Identifier<CHILD>> childrenWithKeyToParent) {
            this.map = childrenWithKeyToParent;
        }

        public Identifier<CHILD> keyToParentOf(Identifier<CHILD> childId) {
            return map.get(childId);
        }
    }

}
