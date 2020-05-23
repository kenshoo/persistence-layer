package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.ChangeEntityCommand;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.Optional.ofNullable;
import static org.jooq.lambda.Seq.seq;


public class IsFieldReferringToParentCommand {

    public static <E extends EntityType<E>> Predicate<EntityField<E, ?>> of(Collection<? extends ChangeEntityCommand<E>> children) {
        EntityType<?> parentType = parentType(children).orElse(null);

        if (parentType == null) {
            return whatever -> false;
        }

        return entityTypeOf(children).getKeyTo(parentType).from()::contains;
    }

    private static <E extends EntityType<E>> E entityTypeOf(Collection<? extends ChangeEntityCommand<E>> children) {
        return seq(children).findFirst().get().getEntityType();
    }

    private static <E extends EntityType<E>> Optional<EntityType> parentType(Collection<? extends ChangeEntityCommand<E>> commands) {
        return seq(commands).findFirst().flatMap(cmd -> ofNullable(cmd.getParent())).map(parent -> parent.getEntityType());
    }

}
