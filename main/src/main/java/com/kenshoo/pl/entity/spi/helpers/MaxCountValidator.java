package com.kenshoo.pl.entity.spi.helpers;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.EntitiesFetcher;
import com.kenshoo.pl.entity.internal.EntityWithNullForMissingField;
import com.kenshoo.pl.entity.spi.ChangesValidator;
import org.jooq.lambda.Seq;
import java.util.Collection;
import java.util.stream.Stream;

import static com.kenshoo.pl.entity.SupportedChangeOperation.CREATE;
import static java.util.Objects.requireNonNull;
import static org.jooq.lambda.Seq.seq;


public class MaxCountValidator<E extends EntityType<E>> implements ChangesValidator<E> {

    private final String errorCode;
    private final EntitiesFetcher fetcher;
    private final UniqueKey<?> groupingKey;
    private final E entityType;
    private final PLCondition condition;
    private final int maxAllowed;

    private MaxCountValidator(E entityType, EntitiesFetcher fetcher, UniqueKey<?> uniqueKey, PLCondition condition, int maxAllowed, String errorCode) {
        this.errorCode = errorCode;
        this.entityType = requireNonNull(entityType, "entityType must be provided");;
        this.fetcher = requireNonNull(fetcher, "entities fetcher must be provided");
        this.groupingKey = requireNonNull(uniqueKey, "grouping key must be provided");
        this.condition = requireNonNull(condition, "condition must be provided");
        if (maxAllowed < 1) {
            throw new IllegalArgumentException("maxAllowed must be greater than zero");
        }
        this.maxAllowed = maxAllowed;
    }

    @Override
    public SupportedChangeOperation getSupportedChangeOperation() {
        return CREATE;
    }

    @Override
    public void validate(Collection<? extends EntityChange<E>> commands, ChangeOperation op, ChangeContext ctx) {

        final var commandsToValidate = seq(commands)
                .filter(command -> condition.getPostFetchCondition().test(ctx.getFinalEntity(command)));

        var groupedCommands = seq(commandsToValidate).groupBy(cmd -> createKeyValue(cmd, ctx, groupingKey));

        var countsInDB = fetcher.fetchCount(entityType, groupedCommands.keySet(), condition);

        groupedCommands.forEach( (groupId, changes) -> {
            var numOfValidCommands = maxAllowed - countsInDB.getOrDefault(groupId, 0);
            seq(changes).skip(numOfValidCommands).forEach(cmd -> markFailure(cmd, ctx));
        });
    }

    private void markFailure(EntityChange<E> cmd, ChangeContext ctx) {
        ctx.addValidationError(cmd, new ValidationError(errorCode));
    }

    private static <E extends EntityType<E>> Identifier<?> createKeyValue(EntityChange<E> cmd, ChangeContext ctx, UniqueKey<?> key) {
        return key.createIdentifier(new EntityWithNullForMissingField(ctx.getFinalEntity(cmd)));
    }

    @Override
    public Stream<? extends EntityField<?, ?>> requiredFields(Collection<? extends EntityField<E, ?>> fieldsToUpdate, ChangeOperation op) {
        return Seq.concat(condition.getFields().stream(), Stream.of(groupingKey.getFields()), Stream.of(groupingKey.getEntityType().getPrimaryKey().getFields()));
    }

    public static class Builder<E extends EntityType<E>> {

        private final E entityType;
        private String errorCode = "MAX_COUNT_EXCEEDED";
        private final EntitiesFetcher fetcher;
        private final UniqueKey<?> groupingKey;
        private PLCondition condition = PLCondition.trueCondition();
        private int maxAllowed;

        public Builder(EntitiesFetcher fetcher, E entityType, UniqueKey<?> groupingKey) {
            this.entityType = entityType;
            this.fetcher = fetcher;
            this.groupingKey = groupingKey;
        }

        public Builder<E> setCondition(PLCondition condition) {
            this.condition = condition;
            return this;
        }

        public Builder<E> setErrorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public Builder<E> setMaxAllowed(int maxAllowed) {
            this.maxAllowed = maxAllowed;
            return this;
        }

        public MaxCountValidator<E> build() {
            return new MaxCountValidator<E>(entityType, fetcher, groupingKey, condition, maxAllowed, errorCode);
        }
    }

}
