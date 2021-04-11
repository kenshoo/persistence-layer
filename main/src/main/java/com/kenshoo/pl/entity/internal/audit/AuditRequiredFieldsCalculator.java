package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.spi.CurrentStateConsumer;
import org.jooq.lambda.Seq;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toSet;
import static org.jooq.lambda.Seq.seq;

public class AuditRequiredFieldsCalculator<E extends EntityType<E>> implements CurrentStateConsumer<E> {

    private final AuditedEntityType<E> auditedEntityType;

    public AuditRequiredFieldsCalculator(final AuditedEntityType<E> auditedEntityType) {
        this.auditedEntityType = requireNonNull(auditedEntityType, "auditedEntityType is required");
    }

    @Override
    public Stream<? extends EntityField<?, ?>> requiredFields(final Collection<? extends EntityField<E, ?>> fieldsToChange,
                                                              final ChangeOperation changeOperation) {

        final Set<? extends EntityField<E, ?>> onChangeFields = auditedEntityType.getOnChangeFields().collect(toSet());

        final Seq<? extends EntityField<E, ?>> intersectedChangeFields =
            seq(fieldsToChange).filter(onChangeFields::contains);

        return Seq.<EntityField<?, ?>>of(auditedEntityType.getIdField())
            .append(auditedEntityType.getMandatoryFields())
            .append(intersectedChangeFields);
    }
}
