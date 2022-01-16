package com.kenshoo.pl.entity.internal.audit;

import com.google.common.annotations.VisibleForTesting;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.annotation.audit.Audited;
import com.kenshoo.pl.entity.spi.audit.AuditFieldValueFormatter;
import com.kenshoo.pl.entity.spi.audit.AuditFieldValueFormatter.MissingAuditFieldValueFormatter;
import com.kenshoo.pl.entity.spi.audit.DefaultAuditFieldValueFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.kenshoo.pl.entity.internal.EntityTypeReflectionUtil.getFieldAnnotation;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public class AuditFieldValueFormatterResolver {

    private static final Logger logger = LoggerFactory.getLogger(AuditFieldValueFormatterResolver.class);

    static final AuditFieldValueFormatterResolver INSTANCE = new AuditFieldValueFormatterResolver();

    private final AuditFieldValueFormatter defaultFormatter;

    private AuditFieldValueFormatterResolver() {
        this(DefaultAuditFieldValueFormatter.INSTANCE);
    }

    @VisibleForTesting
    AuditFieldValueFormatterResolver(final AuditFieldValueFormatter defaultFormatter) {
        this.defaultFormatter = defaultFormatter;
    }

    public <E extends EntityType<E>> AuditFieldValueFormatter resolve(final EntityField<E, ?> field) {
        requireNonNull(field, "field is required");

        return Stream.<Supplier<Optional<AuditFieldValueFormatter>>>of(() -> extractFieldLevelFormatter(field),
                                                                       () -> extractEntityLevelFormatter(field.getEntityType()))
                     .map(Supplier::get)
                     .flatMap(Optional::stream)
                     .findFirst()
                     .orElse(defaultFormatter);
    }

    private <E extends EntityType<E>> Optional<AuditFieldValueFormatter> extractFieldLevelFormatter(final EntityField<E, ?> field) {
        return Optional.ofNullable(getFieldAnnotation(field.getEntityType(), field, Audited.class))
                       .flatMap(this::extractFormatter);
    }

    private Optional<AuditFieldValueFormatter> extractEntityLevelFormatter(final EntityType<?> entityType) {
        return Optional.ofNullable(entityType.getClass().getAnnotation(Audited.class))
                       .flatMap(this::extractFormatter);
    }

    private Optional<AuditFieldValueFormatter> extractFormatter(final Audited auditedAnnotation) {
        return Optional.of(auditedAnnotation.valueFormatter())
                       .filter(not(MissingAuditFieldValueFormatter.class::equals))
                       .flatMap(this::createFormatter);
    }

    private Optional<? extends AuditFieldValueFormatter> createFormatter(final Class<? extends AuditFieldValueFormatter> formatterClass) {
        try {
            return Optional.of(formatterClass.getDeclaredConstructor().newInstance());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            logger.error("Failed to create a formatter of type {} - either it doesn't have a no-arg constructor, or the constructor is not public. Falling back to the default formatter.",
                         formatterClass,
                         e);
            return Optional.empty();
        }
    }
}
