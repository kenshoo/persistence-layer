package com.kenshoo.pl.entity;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.kenshoo.pl.entity.UniqueKeyValue.concat;
import static org.jooq.lambda.Seq.seq;


public class HierarchyKeyPopulator<PARENT extends EntityType<PARENT>> {

    private final CommandToValuesStrategy<PARENT> valueExtractor;
    private final Predicate<EntityField<PARENT, ?>> keyFilter;
    private final Hierarchy hierarchy;

    private HierarchyKeyPopulator(Predicate<EntityField<PARENT, ?>> keyFilter, CommandToValuesStrategy<PARENT> identityValueGetter, Hierarchy hierarchy) {
        this.valueExtractor = identityValueGetter;
        this.keyFilter = keyFilter;
        this.hierarchy = hierarchy;
    }

    public static <E extends EntityType<E>> CommandToValuesStrategy<E> fromContext(ChangeContext ctx) {
        return (fields, cmd) -> {
            CurrentEntityState entity = ctx.getEntity(cmd);
            return fields.stream().map(field -> entity.get(field)).toArray(Object[]::new);
        };
    }

    public static <E extends EntityType<E>> CommandToValuesStrategy<E> fromCommands() {
        return (fields, cmd) -> fields.stream().map(field -> cmd.get(field)).toArray(Object[]::new);
    }

    public void populateKeysToChildren(Collection<? extends ChangeEntityCommand<PARENT>> parents) {

        if (parents.isEmpty()) {
            return;
        }

        hierarchy.childrenTypes(entityType(parents))
                .forEach(populateKeysToChildrenOfSpecificTypeConsumer(parents));
    }

    @SuppressWarnings("unchecked")
    private Consumer<EntityType> populateKeysToChildrenOfSpecificTypeConsumer(Collection<? extends ChangeEntityCommand<PARENT>> parents) {
        return childType -> populateKeysToChildrenOfSpecificType(parents, childType);
    }

    private <CHILD extends EntityType<CHILD>>
    void populateKeysToChildrenOfSpecificType(
            Collection<? extends ChangeEntityCommand<PARENT>> parents,
            CHILD childType) {

        final EntityType.ForeignKey<CHILD, PARENT> childToParentFields = childType.getKeyTo(entityType(parents)).filterByTo(keyFilter);

        if (childToParentFields.notEmpty()) {
            seq(parents).filter(hasAnyChildOf(childType)).forEach(parent -> {
                final Identifier<CHILD> identityValues = parentValues(childToParentFields, valueExtractor, parent);
                parent.getChildren(childType).forEach(child -> child.setKeysToParent(concat(identityValues, child.getKeysToParent())));
            });
        }
    }

    private PARENT entityType(Collection<? extends EntityChange<PARENT>> parents) {
        return first(parents).getEntityType();
    }

    private <CHILD extends EntityType<CHILD>>
    Identifier<CHILD> parentValues(EntityType.ForeignKey<CHILD, PARENT> childToParentKeys, CommandToValuesStrategy commandToValuesStrategy, EntityChange<PARENT> parent) {
        Object[] parentValues = commandToValuesStrategy.getValues(childToParentKeys.to(), parent);
        if (childToParentKeys.size() != parentValues.length) {
            throw new IllegalStateException("Found " + parentValues.length + " values out of " + childToParentKeys.size() + " fields for foreign keys. Keys: " + childToParentKeys);
        }
        return new UniqueKeyValue<>(new UniqueKey<>(array(childToParentKeys.from())), parentValues);
    }

    private <CHILD extends EntityType<CHILD>>
    Predicate<EntityChange<PARENT>> hasAnyChildOf(CHILD childType) {
        return p -> p.getChildren(childType).findAny().isPresent();
    }

    private <T> T first(Iterable<T> items) {
        return items.iterator().next();
    }

    private <CHILD extends EntityType<CHILD>> EntityField<CHILD, ?>[] array(Collection<? extends EntityField<CHILD, ?>> childFields) {
        return childFields.toArray(new EntityField[childFields.size()]);
    }

    public static <PARENT extends EntityType<PARENT>>
    Predicate<EntityField<PARENT, ?>> autoInc() {
        return parentField -> parentField.getDbAdapter().isIdentityField();
    }

    static <PARENT extends EntityType<PARENT>>
    Predicate<EntityField<PARENT, ?>> notAutoInc() {
        return parentField -> !parentField.getDbAdapter().isIdentityField();
    }

    public static <PARENT extends EntityType<PARENT>>
    Predicate<EntityField<PARENT, ?>> anyField() {
        return parentField -> true;
    }

    public static class Builder<E extends EntityType<E>> {

        private CommandToValuesStrategy<E> valueExtractor;
        private Predicate<EntityField<E, ?>> keyFilter;
        private Hierarchy hierarchy;

        public Builder<E> with(Hierarchy hierarchy) {
            this.hierarchy = hierarchy;
            return this;
        }

        public Builder<E> gettingValues(CommandToValuesStrategy<E> valueExtractor) {
            this.valueExtractor = valueExtractor;
            return this;
        }

        public Builder<E> whereParentFieldsAre(Predicate<EntityField<E, ?>> keyFilter) {
            this.keyFilter = keyFilter;
            return this;
        }

        public HierarchyKeyPopulator<E> build() {
            return new HierarchyKeyPopulator<E>(keyFilter, valueExtractor, hierarchy);
        }
    }

}
