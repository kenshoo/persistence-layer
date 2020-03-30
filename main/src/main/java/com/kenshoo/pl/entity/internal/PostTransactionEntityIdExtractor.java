package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.*;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class PostTransactionEntityIdExtractor {

    public static final PostTransactionEntityIdExtractor INSTANCE = new PostTransactionEntityIdExtractor();

    public <E extends EntityType<E>> Optional<? extends Number> extractEntityId(final EntityChange<E> entityChange,
                                                                                final Entity entity) {
        requireNonNull(entityChange, "entityChange is required");
        requireNonNull(entity, "entity is required");

        return entityChange.getEntityType()
                           .getIdField()
                           .flatMap(idField -> extractEntityId(entityChange, entity, idField));
    }

    private <E extends EntityType<E>, T extends Number> Optional<T> extractEntityId(final EntityChange<E> entityChange,
                                                                                    final Entity entity,
                                                                                    final EntityField<E, T> idField) {
        if (entityChange.containsField(idField)) {
            return Optional.ofNullable(entityChange.get(idField));
        }
        final Identifier<E> identifier = entityChange.getIdentifier();
        if (identifier != null && identifier.containsField(idField)) {
            return Optional.ofNullable(identifier.get(idField));
        }
        if (entity.containsField(idField)) {
            return Optional.ofNullable(entity.get(idField));
        }
        return Optional.empty();
    }

    private PostTransactionEntityIdExtractor() {
        // singleton
    }
}
