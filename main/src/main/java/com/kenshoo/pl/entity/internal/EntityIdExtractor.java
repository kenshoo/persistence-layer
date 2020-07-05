package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.*;

import java.util.Optional;

import static com.kenshoo.pl.entity.internal.Triptionals.firstFilled;
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

    private <E extends EntityType<E>, T> Optional<String> extract(final EntityChange<E> entityChange,
                                                                  final Entity entity,
                                                                  final EntityField<E, T> idField) {

        return firstFilled(() -> entityChange.safeGet(idField),
                           () -> extractFromIdentifier(entityChange, idField),
                           () -> entity.safeGet(idField))
            .mapToOptional(String::valueOf);
    }

    private <E extends EntityType<E>, T> Triptional<T> extractFromIdentifier(final EntityChange<E> entityChange,
                                                                             final EntityField<E, T> idField) {
        return Triptional.of(entityChange.getIdentifier())
                         .flatMap(identifier -> identifier.safeGet(idField));
    }

    private EntityIdExtractor() {
        // singleton
    }
}
