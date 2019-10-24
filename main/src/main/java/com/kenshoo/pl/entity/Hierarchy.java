package com.kenshoo.pl.entity;

import org.apache.commons.lang3.tuple.Pair;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;
import static java.util.stream.Collectors.toSet;
import static org.jooq.lambda.Seq.seq;


class Hierarchy {

    private final EntityType<?> root;
    private final Set<Pair<EntityType<?>, EntityType<?>>> parentChildRelations;

    private Hierarchy(EntityType<?> root, Set<Pair<EntityType<?>, EntityType<?>>> parentChildRelations) {
        this.root = root;
        this.parentChildRelations = parentChildRelations;
    }

    static Hierarchy build(ChangeFlowConfig rootFlow) {
        Set<Pair<EntityType<?>, EntityType<?>>> relations = getRelationsRecursively(rootFlow).collect(toSet());
        return new Hierarchy(rootFlow.getEntityType(), relations);
    }

    Collection<? extends EntityType<?>> childrenTypes(EntityType<?> parent) {
        return seq(parentChildRelations)
                .filter(pair -> pair.getLeft().equals(parent))
                .map(Pair::getRight)
                .toList();
    }

    EntityType<?> root() {
        return root;
    }

    boolean contains(EntityField<?, ?> field) {
        return seq(parentChildRelations).anyMatch(
                pair -> field.getEntityType().equals(pair.getLeft()) ||
                        field.getEntityType().equals(pair.getRight())
        );
    }

    private static Stream<Pair<EntityType<?>, EntityType<?>>> getRelationsRecursively(ChangeFlowConfig<?> flow) {
        Stream<Pair<EntityType<?>, EntityType<?>>> myRelations = flow.childFlows().stream().map(childFlow -> Pair.of(flow.getEntityType(), childFlow.getEntityType()));
        return Stream.concat(myRelations, flow.childFlows().stream().flatMap(f -> getRelationsRecursively(f)));
    }

}
