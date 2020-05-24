package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.ChangeEntityCommand;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.Optional.ofNullable;


public class IsFieldReferringToParentCommand {

    public static <E extends EntityType<E>> Predicate<EntityField<E, ?>> of(Collection<? extends ChangeEntityCommand<E>> children) {
        return first(children)
                .flatMap(child -> keyToParent(child))
                .map(key -> key.from()).map(childToParentFields -> (Predicate<EntityField<E, ?>>)childToParentFields::contains)
                .orElse(anyField -> false);
    }

    private static <E extends EntityType<E>> Optional<EntityType.ForeignKey<E, ?>> keyToParent(ChangeEntityCommand<E> child) {
        return ofNullable(child.getParent()).map(parent -> child.getEntityType().getKeyTo(parent.getEntityType()));
    }

    private static <T> Optional<T> first(Collection<T> items) {
        return items.stream().findFirst();
    }

}
