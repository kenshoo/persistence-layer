package com.kenshoo.pl.entity.spi.helpers;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.EntitiesFetcher;
import com.kenshoo.pl.entity.internal.EntityWithNullForMissingField;
import com.kenshoo.pl.entity.spi.ChangesValidator;
import org.apache.commons.lang3.ArrayUtils;
import org.jooq.lambda.Seq;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.kenshoo.pl.entity.ChangeOperation.UPDATE;
import static com.kenshoo.pl.entity.SupportedChangeOperation.CREATE;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;


public class UniquenessValidator<E extends EntityType<E>> implements ChangesValidator<E> {

    private final String errorCode;
    private final EntitiesFetcher fetcher;
    private final UniqueKey<E> uniqueKey;
    private final PLCondition condition;
    private final SupportedChangeOperation operation;

    private UniquenessValidator(EntitiesFetcher fetcher, UniqueKey<E> uniqueKey, SupportedChangeOperation operation, PLCondition condition, String errorCode) {
        this.errorCode = errorCode;
        this.fetcher = requireNonNull(fetcher, "entity fetcher must be provided");
        this.uniqueKey = requireNonNull(uniqueKey, "unique key must be provided");
        this.condition = requireNonNull(condition, "condition must be provided");
        this.operation = requireNonNull(operation, "operation must be provided");
    }

    @Override
    public SupportedChangeOperation getSupportedChangeOperation() {
        return operation;
    }

    @Override
    public void validate(Collection<? extends EntityChange<E>> commands, ChangeOperation op, ChangeContext ctx) {

        final var commandsToValidate = commands.stream().filter(hasChangeInUniqueKeyField()).collect(Collectors.toList());
        Map<Identifier<E>, ? extends EntityChange<E>> commandsByIds = markDuplicatesInCollectionWithErrors(commandsToValidate, ctx);
        if (commandsByIds.isEmpty())
            return;

        UniqueKey<E> pk = uniqueKey.getEntityType().getPrimaryKey();
        EntityField<E, ?>[] uniqueKeyAndPK = ArrayUtils.addAll(uniqueKey.getFields(), pk.getFields());

        Map<Identifier<E>, CurrentEntityState> duplicates = fetcher.fetch(uniqueKey.getEntityType(), commandsByIds.keySet(), condition, uniqueKeyAndPK)
                .stream()
                .collect(toMap(e -> createKeyValue(e, uniqueKey), identity()));

        duplicates.forEach((dupKey, dupEntity) -> ctx.addValidationError(commandsByIds.get(dupKey), errorForDatabaseConflict(dupEntity, pk)));
    }

    private Predicate<EntityChange<E>> hasChangeInUniqueKeyField() {
        return cmd -> !UPDATE.equals(cmd.getChangeOperation()) || Arrays.stream(uniqueKey.getFields()).anyMatch(cmd::isFieldChanged);
    }

    private Map<Identifier<E>, EntityChange<E>> markDuplicatesInCollectionWithErrors(Collection<? extends EntityChange<E>> commands, ChangeContext ctx) {
        return commands
                .stream()
                .filter(command -> condition.getPostFetchCondition().test(ctx.getFinalEntity(command)))
                .collect(toMap(cmd -> createKeyValue(cmd, ctx, uniqueKey), identity(), fail2ndConflictingCommand(ctx)));
    }

    private ValidationError errorForDatabaseConflict(CurrentEntityState dupEntity, UniqueKey<E> pk) {
        return new ValidationError(errorCode, Seq.of(pk.getFields()).toMap(Object::toString, field -> String.valueOf(dupEntity.get(field))));
    }

    private BinaryOperator<EntityChange<E>> fail2ndConflictingCommand(ChangeContext ctx) {
        return (cmd1, cmd2) -> {
            ctx.addValidationError(cmd2, new ValidationError(errorCode));
            return cmd1;
        };
    }

    private static <E extends EntityType<E>> Identifier<E> createKeyValue(EntityChange<E> cmd, ChangeContext ctx, UniqueKey<E> key) {
        return key.createIdentifier(new EntityWithNullForMissingField(ctx.getFinalEntity(cmd)));
    }

    private Identifier<E> createKeyValue(CurrentEntityState currentEntityState, UniqueKey<E> key) {
        return key.createIdentifier(currentEntityState);
    }

    @Override
    public Stream<? extends EntityField<?, ?>> requiredFields(Collection<? extends EntityField<E, ?>> fieldsToUpdate, ChangeOperation op) {
        return Seq.concat(condition.getFields().stream(), Stream.of(uniqueKey.getFields()), Stream.of(uniqueKey.getEntityType().getPrimaryKey().getFields()));
    }

    public static class Builder<E extends EntityType<E>> {

        private String errorCode = "DUPLICATE_ENTITY";
        private final EntitiesFetcher fetcher;
        private final UniqueKey<E> uniqueKey;
        private PLCondition condition = PLCondition.trueCondition();
        private SupportedChangeOperation operation = CREATE;

        public Builder(EntitiesFetcher fetcher, UniqueKey<E> uniqueKey) {
            this.fetcher = fetcher;
            this.uniqueKey = uniqueKey;
        }

        public Builder<E> setOperation(SupportedChangeOperation operation) {
            this.operation = operation;
            return this;
        }

        public Builder<E> setCondition(PLCondition condition) {
            this.condition = condition;
            return this;
        }

        public Builder<E> setErrorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public UniquenessValidator<E> build() {
            return new UniquenessValidator<>(fetcher, uniqueKey, operation, condition, errorCode);
        }
    }

}
