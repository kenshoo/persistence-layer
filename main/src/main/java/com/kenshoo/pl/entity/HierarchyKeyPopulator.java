package com.kenshoo.pl.entity;

import com.kenshoo.pl.entity.internal.EntityDbUtil;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import static com.kenshoo.pl.entity.ChangeOperation.CREATE;
import static org.jooq.lambda.Seq.seq;


public class HierarchyKeyPopulator {

    private final CommandToValuesStrategy identityValueGetter;

    private HierarchyKeyPopulator() {
        this.identityValueGetter = null;
    }

    private HierarchyKeyPopulator(CommandToValuesStrategy identityValueGetter) {
        this.identityValueGetter = identityValueGetter;
    }

    public static HierarchyKeyPopulator withoutHandlingIdentityFields() {
        return new HierarchyKeyPopulator();
    }

    public static HierarchyKeyPopulator takingIdentityValuesFromContext() {
        return new HierarchyKeyPopulator(takingValuesFromContext());
    }

    public static <E extends EntityType<E>> CommandToValuesStrategy<E> takingValuesFromContext() {
        return (fields, cmd, ctx) -> EntityDbUtil.getFieldValues(fields, ctx.getEntity(cmd));
    }

    public static <E extends EntityType<E>> CommandToValuesStrategy<E> takeCommandValuesOnCreateAndContextValuesOnUpdate() {
        return (fields, cmd, ctx) -> cmd.getChangeOperation() == CREATE
                ? EntityDbUtil.getFieldValues(fields, cmd)
                : EntityDbUtil.getFieldValues(fields, ctx.getEntity(cmd));
    }

    public <PARENT extends EntityType<PARENT>>
    void populateKeysToChildren(
            Collection<? extends EntityChange<PARENT>> parents,
            ChangeContext context) {

        if (parents.isEmpty()) {
            return;
        }

        context.getHierarchy().childrenTypes(first(parents).getEntityType())
                .forEach(populateKeysToChildrenOfSpecificTypeConsumer(parents, context));
    }

    @SuppressWarnings("unchecked")
    private <PARENT extends EntityType<PARENT>> Consumer<EntityType> populateKeysToChildrenOfSpecificTypeConsumer(Collection<? extends EntityChange<PARENT>> parents, ChangeContext context) {
        return childType -> populateKeysToChildrenOfSpecificType(parents, childType, context);
    }

    private <PARENT extends EntityType<PARENT>, CHILD extends EntityType<CHILD>>
    void populateKeysToChildrenOfSpecificType(
            Collection<? extends EntityChange<PARENT>> parents,
            CHILD childType,
            ChangeContext context) {

        final EntityType.ForeignKey<CHILD, PARENT> allChildToParentFields = childType.getKeyTo(entityType(parents));
        final EntityType.ForeignKey<CHILD, PARENT> childToParentIdentityFields = allChildToParentFields.filter(parentFieldIsAutoIncrementing());
        final EntityType.ForeignKey<CHILD, PARENT> childToParentNonIdentityFields = allChildToParentFields.filter(parentFieldIsNotAutoIncrementing());

        seq(parents).filter(hasAnyChildOf(childType)).forEach(parent -> {
            final UniqueKeyValue<CHILD> identityValues = Optional.ofNullable(identityValueGetter).map(valueGetter -> parentValues(context, childToParentIdentityFields, valueGetter, parent)).orElse(UniqueKeyValue.empty());
            final UniqueKeyValue<CHILD> nonIdentityValues = parentValues(context, childToParentNonIdentityFields, takeCommandValuesOnCreateAndContextValuesOnUpdate(), parent);
            Identifier<CHILD> allKeys = identityValues.concat(nonIdentityValues);
            if (!allKeys.isEmpty()) {
                parent.getChildren(childType).forEach(child -> child.setKeysToParent(allKeys));
            }
        });

    }

    private <PARENT extends EntityType<PARENT>> PARENT entityType(Collection<? extends EntityChange<PARENT>> parents) {
        return first(parents).getEntityType();
    }

    private <PARENT extends EntityType<PARENT>, CHILD extends EntityType<CHILD>>
    UniqueKeyValue<CHILD> parentValues(ChangeContext context, EntityType.ForeignKey<CHILD, PARENT> childToParentKeys, CommandToValuesStrategy commandToValuesStrategy, EntityChange<PARENT> parent) {
        Object[] parentValues = commandToValuesStrategy.getValues(childToParentKeys.to(), parent, context);
        if (childToParentKeys.size() != parentValues.length) {
            throw new IllegalStateException("Found " + parentValues.length + " values out of " + childToParentKeys.size() + " fields for foreign keys. Keys: " + childToParentKeys);
        }
        return new UniqueKeyValue<>(new UniqueKey<>(array(childToParentKeys.from())), parentValues);
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

    private <CHILD extends EntityType<CHILD>> EntityField<CHILD, ?>[] array(Collection<? extends EntityField<CHILD, ?>> childFields) {
        return childFields.toArray(new EntityField[childFields.size()]);
    }

}
