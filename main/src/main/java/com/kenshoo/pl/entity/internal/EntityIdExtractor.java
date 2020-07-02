package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.*;
import org.jooq.lambda.Seq;

import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class EntityIdExtractor {

    public static final EntityIdExtractor INSTANCE = new EntityIdExtractor();

    public <E extends EntityType<E>> Optional<String> extract(final EntityChange<E> entityChange,
                                                              final Entity entity) {
        requireNonNull(entityChange, "entityChange is required");
        requireNonNull(entity, "entity is required");

        return entityChange.getEntityType()
                           .getIdField()
                           .flatMap(idField -> extract(entityChange, entity, idField));
    }

    private <E extends EntityType<E>> Optional<String> extract(final EntityChange<E> entityChange,
                                                               final Entity entity,
                                                               final EntityField<E, ?> idField) {

        return Seq.<Supplier<Triptional<?>>>of(() -> entityChange.safeGet(idField),
                                               () -> extractFromIdentifier(entityChange, idField),
                                               () -> entity.safeGet(idField))
            .map(Supplier::get)
            .findFirst(Triptional::isFilled)
            .flatMap(triptionalId -> triptionalId.mapToOptional(String::valueOf));
    }

    private <E extends EntityType<E>> Triptional<?> extractFromIdentifier(final EntityChange<E> entityChange,
                                                                          final EntityField<E, ?> idField) {
        return Triptional.of(entityChange.getIdentifier())
                         .flatMap(identifier -> identifier.safeGet(idField));
    }

    private EntityIdExtractor() {
        // singleton
    }
}
