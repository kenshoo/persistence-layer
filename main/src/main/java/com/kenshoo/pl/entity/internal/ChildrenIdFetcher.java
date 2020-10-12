package com.kenshoo.pl.entity.internal;

import com.google.common.collect.Iterables;
import com.kenshoo.pl.entity.UniqueKey;
import com.kenshoo.pl.entity.*;
import org.jooq.lambda.Seq;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import static org.jooq.lambda.Seq.seq;


public class ChildrenIdFetcher<PARENT extends EntityType<PARENT>, CHILD extends EntityType<CHILD>> {

    private final PLContext plContext;

    public ChildrenIdFetcher(PLContext plContext) {
        this.plContext = plContext;
    }

    public Stream<FullIdentifier<PARENT, CHILD>>
    fetch(Collection<? extends Identifier<PARENT>> parentIds, UniqueKey<CHILD> childKey) {

        if (parentIds.isEmpty()) {
            return Stream.empty();
        }

        final PARENT parentType = first(parentIds).getUniqueKey().getEntityType();
        final CHILD childType = childKey.getEntityType();
        final EntityType.ForeignKey<CHILD, PARENT> keyToParent = childType.getKeyTo(parentType);
        final UniqueKey<PARENT> parentKey = first(parentIds).getUniqueKey();
        final UniqueKey<CHILD> childFK = new UniqueKey<>(keyToParent.from());

        final EntityField<?, ?>[] requestedFields = Iterables.toArray(Seq.concat(
                Seq.of(parentKey.getFields()),
                Seq.of(childKey.getFields()),
                Seq.of(childFK.getFields())), EntityField.class);

        List<CurrentEntityState> entities = plContext.select(requestedFields)
                .from(childType)
                .where(PLCondition.trueCondition())
                .fetchByKeys(parentIds);

        return fullIdentifierOf(entities, parentKey, childKey, childFK);
    }

    private Stream<FullIdentifier<PARENT, CHILD>> fullIdentifierOf(List<CurrentEntityState> entities, UniqueKey<PARENT> parentKey, UniqueKey<CHILD> childKey, UniqueKey<CHILD> childFK) {
        return seq(entities)
                .map(entity ->
                        new FullIdentifier<>(
                                parse(parentKey, entity),
                                parse(childKey, entity),
                                parse(childFK, entity)
                        ));
    }

    private <T extends EntityType<T>> Identifier<T> parse(UniqueKey<T> key, CurrentEntityState entity) {
        return key.createIdentifier(entity);
    }

    private <T> T first(Collection<T> collection) {
        return collection.iterator().next();
    }
}
