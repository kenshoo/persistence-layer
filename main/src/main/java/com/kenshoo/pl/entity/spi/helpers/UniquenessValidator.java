package com.kenshoo.pl.entity.spi.helpers;

import com.kenshoo.pl.entity.ChangeContext;
import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.Entity;
import com.kenshoo.pl.entity.EntityChange;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.PLCondition;
import com.kenshoo.pl.entity.SupportedChangeOperation;
import com.kenshoo.pl.entity.UniqueKey;
import com.kenshoo.pl.entity.UniqueKeyValue;
import com.kenshoo.pl.entity.ValidationError;
import com.kenshoo.pl.entity.internal.EntitiesFetcher;
import com.kenshoo.pl.entity.spi.ChangesValidator;
import org.apache.commons.lang3.ArrayUtils;
import org.jooq.lambda.Seq;

import java.util.Collection;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

import static com.kenshoo.pl.entity.SupportedChangeOperation.CREATE;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;


public class UniquenessValidator<E extends EntityType<E>> implements ChangesValidator<E> {

    private final String errorCode;
    private final EntitiesFetcher fetcher;
    private final UniqueKey<E> uniqueKey;
    private final PLCondition condition;

    private UniquenessValidator(EntitiesFetcher fetcher, UniqueKey<E> uniqueKey, PLCondition condition, String errorCode) {
        this.errorCode = errorCode;
        this.fetcher = requireNonNull(fetcher, "entity fetcher must be provided");
        this.uniqueKey = requireNonNull(uniqueKey, "unique key must be provided");
        this.condition = requireNonNull(condition, "condition must be provided");
    }

    @Override
    public SupportedChangeOperation getSupportedChangeOperation() {
        return CREATE;
    }

    @Override
    public void validate(Collection<? extends EntityChange<E>> commands, ChangeOperation op, ChangeContext ctx) {

        Map<Identifier<E>, ? extends EntityChange<E>> commandsByIds = markDuplicatesInCollectionWithErrors(commands, ctx);

        UniqueKey<E> pk = uniqueKey.getEntityType().getPrimaryKey();
        EntityField<E, ?>[] uniqueKeyAndPK = ArrayUtils.addAll(uniqueKey.getFields(), pk.getFields());

        Map<Identifier<E>, Entity> duplicates = fetcher.fetch(uniqueKey.getEntityType(), commandsByIds.keySet(), condition, uniqueKeyAndPK)
                .stream().collect(toMap(e -> createKeyValue(e, uniqueKey), identity()));

        duplicates.forEach((dupKey, dupEntity) -> ctx.addValidationError(commandsByIds.get(dupKey), errorForDatabaseConflict(dupEntity, pk)));
    }

    private Map<Identifier<E>, EntityChange<E>> markDuplicatesInCollectionWithErrors(Collection<? extends EntityChange<E>> commands, ChangeContext ctx) {
        return commands
                .stream()
                .collect(toMap(cmd -> createKeyValue(cmd, uniqueKey), identity(), fail2ndConflictingCommand(ctx)));
    }

    private ValidationError errorForDatabaseConflict(Entity dupEntity, UniqueKey<E> pk) {
        return new ValidationError(errorCode, Seq.of(pk.getFields()).toMap(Object::toString, field -> String.valueOf(dupEntity.get(field))));
    }

    private BinaryOperator<EntityChange<E>> fail2ndConflictingCommand(ChangeContext ctx) {
        return (cmd1, cmd2) -> {
            ctx.addValidationError(cmd2, new ValidationError(errorCode));
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
        return Stream.of(uniqueKey.getEntityType().getPrimaryKey().getFields());
    }

    public static class Builder<E extends EntityType<E>> {

        private String errorCode = "DUPLICATE_ENTITY";
        private final EntitiesFetcher fetcher;
        private final UniqueKey<E> uniqueKey;
        private PLCondition condition = PLCondition.trueCondition();

        public Builder(EntitiesFetcher fetcher, UniqueKey<E> uniqueKey) {
            this.fetcher = fetcher;
            this.uniqueKey = uniqueKey;
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
            return new UniquenessValidator<>(fetcher, uniqueKey, condition, errorCode);
        }
    }

}
