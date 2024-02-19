package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.*;

import java.util.Optional;

import static com.kenshoo.pl.entity.internal.Triptionals.firstPresent;
import static java.util.Objects.requireNonNull;

public class EntityIdExtractor {

    public static final EntityIdExtractor INSTANCE = new EntityIdExtractor();

    public <E extends EntityType<E>> Optional<String> extract(final EntityChange<E> entityChange,
                                                              final CurrentEntityState currentState) {
        requireNonNull(entityChange, "entityChange is required");
        requireNonNull(currentState, "currentState is required");

        return entityChange.getEntityType()
                           .getIdField()
                           .flatMap(idField -> extract(entityChange, currentState, idField));
    }

    private <E extends EntityType<E>, T> Optional<String> extract(final EntityChange<E> entityChange,
                                                                  final CurrentEntityState currentState,
                                                                  final EntityField<E, T> idField) {

        return firstPresent(() -> extractFromIdentifier(entityChange, idField),
                            () -> FinalEntityState.merge(currentState, entityChange).safeGet(idField))
            .mapToOptional(String::valueOf);
    }

    private <E extends EntityType<E>, T> Triptional<T> extractFromIdentifier(final EntityChange<E> entityChange,
                                                                             final EntityField<E, T> idField) {
        return Triptional.of(entityChange.getIdentifier())
                         .flatMap(identifier -> identifier.safeGet(idField), Triptional::absent);
    }

    private EntityIdExtractor() {
        // singleton
    }
}
