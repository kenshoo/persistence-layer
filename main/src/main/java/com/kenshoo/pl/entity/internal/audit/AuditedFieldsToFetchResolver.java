package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import org.jooq.lambda.Seq;

import java.util.Collection;
import java.util.stream.Stream;

import static org.jooq.lambda.Seq.seq;

public class AuditedFieldsToFetchResolver {

    static final AuditedFieldsToFetchResolver INSTANCE = new AuditedFieldsToFetchResolver();

    public <E extends EntityType<E>> Stream<? extends EntityField<?, ?>> resolve(
        final AuditedFieldSet<E> auditedFieldSet,
        final Collection<? extends EntityField<E, ?>> fieldsToChange) {

        final Seq<? extends EntityField<E, ?>> intersectedChangeFields =
            seq(fieldsToChange).filter(auditedFieldSet.getOnChangeFields()::contains);

        return Seq.<EntityField<?, ?>>of(auditedFieldSet.getIdField())
            .append(auditedFieldSet.getAllMandatoryFields())
            .append(intersectedChangeFields);
    }

    private AuditedFieldsToFetchResolver() {
        // singleton
    }
}
