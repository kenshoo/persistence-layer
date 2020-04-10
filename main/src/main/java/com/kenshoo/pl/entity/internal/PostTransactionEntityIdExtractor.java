package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.*;
import org.jooq.lambda.Seq;

import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class PostTransactionEntityIdExtractor {

    public static final PostTransactionEntityIdExtractor INSTANCE = new PostTransactionEntityIdExtractor();

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

        return Seq.<Supplier<Optional<?>>>of(() -> extractFromEntityChange(entityChange, idField),
                                             () -> extractFromIdentifier(entityChange, idField),
                                             () -> extractFromEntity(entity, idField))
            .map(Supplier::get)
            .findFirst(Optional::isPresent)
            .flatMap(optionalId -> optionalId.map(String::valueOf));
    }

    private <E extends EntityType<E>> Optional<?> extractFromEntityChange(final EntityChange<E> entityChange,
                                                                          final EntityField<E, ?> idField) {
        if (entityChange.containsField(idField)) {
            return Optional.ofNullable(entityChange.get(idField));
        }
        return Optional.empty();
    }

    private <E extends EntityType<E>> Optional<?> extractFromIdentifier(EntityChange<E> entityChange, EntityField<E, ?> idField) {
        final Identifier<E> identifier = entityChange.getIdentifier();
        if (identifier != null && identifier.containsField(idField)) {
            return Optional.ofNullable(identifier.get(idField));
        }
        return Optional.empty();
    }

    private <E extends EntityType<E>> Optional<?> extractFromEntity(Entity entity, EntityField<E, ?> idField) {
        if (entity.containsField(idField)) {
            return Optional.ofNullable(entity.get(idField));
        }
        return Optional.empty();
    }

    private PostTransactionEntityIdExtractor() {
        // singleton
    }
}
