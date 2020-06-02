package com.kenshoo.pl.entity.spi.helpers;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.EntitiesFetcher;
import com.kenshoo.pl.entity.spi.ChangesValidator;
import org.apache.commons.lang3.ArrayUtils;
import java.util.Collection;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

import static com.kenshoo.pl.entity.SupportedChangeOperation.CREATE;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;


public class UniquenessValidator<E extends EntityType<E>> implements ChangesValidator<E> {

    private final EntitiesFetcher fetcher;
    private final UniqueKey<E> uniqueKey;
    private final PLCondition condition;
    private final ErrorFormatter<E> errorFormatter;

    public interface ErrorFormatter<E extends EntityType<E>> {
        ValidationError errorForDatabaseConflict(UniqueKey<E> uniqueKey, Identifier<E> idOfDupEntity, EntityChange<E> cmd);
        ValidationError errorForChunkConflict(UniqueKey<E> uniqueKey, EntityChange<E> cmd);
    }

    private UniquenessValidator(EntitiesFetcher fetcher, UniqueKey<E> uniqueKey, PLCondition condition, ErrorFormatter<E> errorFormatter) {
        this.fetcher = requireNonNull(fetcher, "entity fetcher must b provided");
        this.uniqueKey = requireNonNull(uniqueKey, "unique key must be provided");
        this.condition = requireNonNull(condition, "condition must be provided");
        this.errorFormatter = requireNonNull(errorFormatter, "error formatter must be provided");
    }

    @Override
    public SupportedChangeOperation getSupportedChangeOperation() {
        return CREATE;
    }

    @Override
    public void validate(Collection<? extends EntityChange<E>> commands, ChangeOperation op, ChangeContext ctx) {

        Map<Identifier<E>, ? extends EntityChange<E>> commandsByIds = commands
                .stream()
                .collect(toMap(cmd -> createKeyValue(cmd, uniqueKey), identity(), fail2ndConflictingCommand(ctx)));

        UniqueKey<E> pk = uniqueKey.getEntityType().getPrimaryKey();
        EntityField<E, ?>[] uniqueKeyAndPK = ArrayUtils.addAll(uniqueKey.getFields(), pk.getFields());

        Map<Identifier<E>, Entity> duplicates = fetcher.fetch(uniqueKey.getEntityType(), commandsByIds.keySet(), condition, uniqueKeyAndPK)
                .stream().collect(toMap(e -> createKeyValue(e, uniqueKey), identity()));

        duplicates.forEach((dupKey, dupEntity) -> ctx.addValidationError(
                commandsByIds.get(dupKey),
                errorFormatter.errorForDatabaseConflict(uniqueKey, createKeyValue(dupEntity, uniqueKey), commandsByIds.get(dupKey))
        ));
    }

    private BinaryOperator<EntityChange<E>> fail2ndConflictingCommand(ChangeContext ctx) {
        return (cmd1, cmd2) -> {
            ctx.addValidationError(cmd2, errorFormatter.errorForChunkConflict(uniqueKey, cmd2));
            return cmd1;
        };
    }

    private static <E extends EntityType<E>> Identifier<E> createKeyValue(EntityChange<E> cmd, UniqueKey<E> key) {
        Object[] values = Stream.of(key.getFields()).map(cmd::get).toArray();
        return new UniqueKeyValue<>(key, values);
    }

    private Identifier<E> createKeyValue(Entity cmd, UniqueKey<E> key) {
        Object[] values = Stream.of(key.getFields()).map(cmd::get).toArray();
        return new UniqueKeyValue<>(key, values);
    }

    @Override
    public Stream<? extends EntityField<?, ?>> requiredFields(Collection<? extends EntityField<E, ?>> fieldsToUpdate, ChangeOperation op) {
        return Stream.of();
    }

    public static class DefaultErrorFormatter<E extends EntityType<E>> implements ErrorFormatter<E> {

        @Override
        public ValidationError errorForDatabaseConflict(UniqueKey<E> uniqueKey, Identifier<E> idOfDupEntity, EntityChange<E> cmd) {
            return new ValidationError("Command for " + uniqueKey.getEntityType() + " is conflicting with existing entity " + idOfDupEntity + " on " + uniqueKey);
        }

        @Override
        public ValidationError errorForChunkConflict(UniqueKey<E> uniqueKey, EntityChange<E> cmd) {
            return new ValidationError("Command for " + uniqueKey.getEntityType() + " is conflicting with another command on " + createKeyValue(cmd, uniqueKey));
        }
    }

    public static class Builder<E extends EntityType<E>> {

        private final EntitiesFetcher fetcher;
        private final UniqueKey<E> uniqueKey;
        private PLCondition condition = PLCondition.TrueCondition;
        private ErrorFormatter<E> errorFormatter = new DefaultErrorFormatter<>();

        public Builder(EntitiesFetcher fetcher, UniqueKey<E> uniqueKey) {
            this.fetcher = fetcher;
            this.uniqueKey = uniqueKey;
        }

        public Builder<E> setCondition(PLCondition condition) {
            this.condition = condition;
            return this;
        }

        public Builder<E> setErrorFormatter(ErrorFormatter<E> errorFormatter) {
            this.errorFormatter = errorFormatter;
            return this;
        }

        public UniquenessValidator<E> build() {
            return new UniquenessValidator<>(fetcher, uniqueKey, condition, errorFormatter);
        }
    }

}
