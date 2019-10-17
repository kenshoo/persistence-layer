package com.kenshoo.pl.entity;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static com.kenshoo.pl.entity.CommandToValuesStrategies.takeCommandValuesOnCreateAndContextValuesOnUpdate;
import static org.jooq.lambda.Seq.seq;


public class HierarchyKeyPopulator {

    private final CommandToValuesStrategy identityValueGetter;
    private final CommandToValuesStrategy nonIdentityValueGetter = takeCommandValuesOnCreateAndContextValuesOnUpdate();

    private HierarchyKeyPopulator(CommandToValuesStrategy identityValueGetter) {
        this.identityValueGetter = identityValueGetter;
    }

    public static HierarchyKeyPopulator whenGettingIdentityFields(CommandToValuesStrategy identityValueGetter) {
        return new HierarchyKeyPopulator(identityValueGetter);
    }

    public <PARENT extends EntityType<PARENT>>
    void populateKeysToChildren(
            Collection<? extends EntityChange<PARENT>> parents,
            ChangeContext context) {

        if (parents.isEmpty()) {
            return;
        }

        context.getHierarchy().childrenTypes(first(parents).getEntityType())
                .forEach(childType -> populateKeysToChildrenOfSpecificType(parents, childType, context));
    }

    private <PARENT extends EntityType<PARENT>, CHILD extends EntityType<CHILD>>
    void populateKeysToChildrenOfSpecificType(
            Collection<? extends EntityChange<PARENT>> parents,
            CHILD childType,
            ChangeContext context) {

        final EntityType.ForeignKey<CHILD, PARENT> allChildToParentFields = childType.getKeyTo(entityType(parents));
        final EntityType.ForeignKey<CHILD, PARENT> childToParentIdentityFields = allChildToParentFields.where(parentFieldIsAutoIncrementing());
        final EntityType.ForeignKey<CHILD, PARENT> childToParentNonIdentityFields = allChildToParentFields.where(parentFieldIsNotAutoIncrementing());

        seq(parents).filter(hasAnyChildOf(childType)).forEach(parent -> {
            final UniqueKeyValue<CHILD> identityValues = parentValues(context, childToParentIdentityFields, identityValueGetter, parent);
            final UniqueKeyValue<CHILD> nonIdentityValues = parentValues(context, childToParentNonIdentityFields, nonIdentityValueGetter, parent);
            parent.getChildren(childType).forEach(child -> child.setKeysToParent(identityValues.concat(nonIdentityValues)));
        });

    }

    private <PARENT extends EntityType<PARENT>> PARENT entityType(Collection<? extends EntityChange<PARENT>> parents) {
        return first(parents).getEntityType();
    }

    private <PARENT extends EntityType<PARENT>, CHILD extends EntityType<CHILD>>
    UniqueKeyValue<CHILD> parentValues(ChangeContext context, EntityType.ForeignKey<CHILD, PARENT> childToParentKeys, CommandToValuesStrategy commandToValuesStrategy, EntityChange<PARENT> parent) {
        Optional<Object[]> parentValues = commandToValuesStrategy.getValues(childToParentKeys.to, parent, context);
        return parentValues.map(values -> {
            if (childToParentKeys.to.size() != values.length) {
                throw new IllegalStateException("Found " + values.length + " values out of " + childToParentKeys.to.size() + " fields for foreign keys. Keys: " + childToParentKeys);
            }
            return new UniqueKeyValue<>(new UniqueKey<>(array(childToParentKeys.from)), values);
        }).orElse(UniqueKeyValue.empty());
    }

    private <PARENT extends EntityType<PARENT>, CHILD extends EntityType<CHILD>>
    BiPredicate<EntityField<CHILD, ?>, EntityField<PARENT, ?>> parentFieldIsAutoIncrementing() {
        return (__, parentField) -> parentField.getDbAdapter().isIdentityField();
    }

    private <PARENT extends EntityType<PARENT>, CHILD extends EntityType<CHILD>>
    BiPredicate<EntityField<CHILD, ?>, EntityField<PARENT, ?>> parentFieldIsNotAutoIncrementing() {
        return (__, parentField) -> !parentField.getDbAdapter().isIdentityField();
    }

    private <PARENT extends EntityType<PARENT>, CHILD extends EntityType<CHILD>>
    Predicate<EntityChange<PARENT>> hasAnyChildOf(CHILD childType) {
        return p -> p.getChildren(childType).findAny().isPresent();
    }

    private <T> T first(Iterable<T> items) {
        return items.iterator().next();
    }

    private <CHILD extends EntityType<CHILD>> EntityField<CHILD, ?>[] array(Collection<EntityField<CHILD, ?>> childFields) {
        return childFields.toArray(new EntityField[childFields.size()]);
    }

}
