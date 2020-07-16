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

public class DeletionCommandPopulator {

    private final ChildrenIdFetcher childrenIdFetcher;

    public DeletionCommandPopulator(PLContext plContext) {
        childrenIdFetcher = new ChildrenIdFetcher(plContext);
    }

    @VisibleForTesting
    public DeletionCommandPopulator(ChildrenIdFetcher childrenIdFetcher) {
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
        addDeletionChildCommands(seq(parents).filter(this::isCascadeDeletion), childType, childrenFromDB);
        supplyChildCommands(seq(parents).filter(this::withMissingChildSupplier), childType, childrenFromDB);
        populateKeyToParent(parents, childType, childrenFromDB);
        handleRecursive(seq(parents).flatMap(p -> p.getChildren(childType)).filter(child -> child.getKeysToParent() != null), childFlow);
    }

    private <PARENT extends EntityType<PARENT>, CHILD extends EntityType<CHILD>>
    ChildrenFromDB<PARENT, CHILD> getExistingChildrenFromDB(Collection<? extends ChangeEntityCommand<PARENT>> parents, CHILD childType) {
        List<Identifier<PARENT>> parentIds = seq(parents).map(p -> concatenatedId(p)).toList();
        final UniqueKey<CHILD> childKey = identifierOfFirstChildCmd(parents, childType).orElseGet(childType::getPrimaryKey);
        return new ChildrenFromDB<>(childrenIdFetcher.fetch(parentIds, childKey));
    }

    private <PARENT extends EntityType<PARENT>, CHILD extends EntityType<CHILD>>
    void supplyChildCommands(Stream<? extends ChangeEntityCommand<PARENT>> parents, CHILD childType, ChildrenFromDB<PARENT, CHILD> childrenFromDB) {
        parents.forEach(parent -> {
            final Set<Identifier<CHILD>> childrenFromCommand = childrenIdsOf(childType, parent);
            seq(childrenFromDB.getChildIds(parent))
                    .filter(childIds -> !childrenFromCommand.contains(childIds))
                    .forEach(missingChildIds -> {
                        parent.getMissingChildrenSupplier(childType).flatMap(s -> s.supplyNewCommand(missingChildIds)).ifPresent(newCmd -> {
                            parent.addChild(newCmd);
                        });
                    });
        });
    }

    private <PARENT extends EntityType<PARENT>, CHILD extends EntityType<CHILD>>
    void addDeletionChildCommands(Stream<? extends ChangeEntityCommand<PARENT>> parents, CHILD childType, ChildrenFromDB<PARENT, CHILD> childrenFromDB) {
        parents.forEach(parent ->
            childrenFromDB.getChildIds(parent)
                    .forEach(childId -> {
                        DeleteEntityCommand<CHILD, ? extends Identifier<CHILD>> newCmd = new DeleteEntityCommand<>(childType, childId).setCascade();
                        parent.addChild(newCmd);
                    })
        );
    }

    private <PARENT extends EntityType<PARENT>, CHILD extends EntityType<CHILD>>
    void populateKeyToParent(Collection<? extends ChangeEntityCommand<PARENT>> parents, CHILD childType, ChildrenFromDB<PARENT, CHILD> childrenFromDB) {
        seq(parents).forEach(parent -> {
            ChildrenWithKeyToParent<CHILD> childrenOfParent = childrenFromDB.of(parent);
            parent.getChildren(childType).forEach(child -> child.setKeysToParent(childrenOfParent.keyToParentOf(child.getIdentifier())));
        });
    }

    private static <E extends EntityType<E>> Identifier<E> concatenatedId(ChangeEntityCommand<E> currentState) {
        return concat(currentState.getIdentifier(),  currentState.getKeysToParent());
    }

    private <PARENT extends EntityType<PARENT>, CHILD extends EntityType<CHILD>>
    Set<Identifier<CHILD>> childrenIdsOf(CHILD childType, ChangeEntityCommand<PARENT> parent) {
        return parent.getChildren(childType).map(EntityChange::getIdentifier)
                .filter(Objects::nonNull)
                .collect(toSet());
    }

    private <E extends EntityType<E>>
    boolean recursivelyCheckIfAnyShouldCascade(ChangeEntityCommand<E> parent) {
        return withMissingChildSupplier(parent) || isCascadeDeletion(parent) || parent.getChildren().anyMatch(child -> recursivelyCheckIfAnyShouldCascade(child));
    }

    private <E extends EntityType<E>>
    boolean isCascadeDeletion(ChangeEntityCommand<E> cmd) {
        return cmd instanceof DeleteEntityCommand && ((DeleteEntityCommand) cmd).isCascade();
    }

    private <PARENT extends EntityType<PARENT>> boolean withMissingChildSupplier(ChangeEntityCommand<PARENT> cmd) {
        return !cmd.getMissingChildrenSuppliers().isEmpty();
    }

    private <PARENT extends EntityType<PARENT>, CHILD extends EntityType<CHILD>>
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
        }

        ChildrenWithKeyToParent<CHILD> of(ChangeEntityCommand<PARENT> parent) {
            return map.getOrDefault(concatenatedId(parent), EMPTY);
        }

        Set<Identifier<CHILD>> getChildIds(ChangeEntityCommand<PARENT> parent) {
            return of(parent).map.keySet();
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
