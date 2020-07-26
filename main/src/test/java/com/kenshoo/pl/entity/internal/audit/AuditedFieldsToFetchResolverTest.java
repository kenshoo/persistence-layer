package com.kenshoo.pl.entity.internal.audit;

import com.google.common.collect.ImmutableSet;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedAncestorType;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.kenshoo.pl.entity.internal.audit.AuditedFieldSet.builder;
import static com.kenshoo.pl.entity.internal.audit.AuditedFieldsToFetchResolver.INSTANCE;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AuditedFieldsToFetchResolverTest {

    @Test
    public void resolve_FieldSetHasIdOnly_FieldsToChangeAreDifferent_ShouldReturnIdOnly() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID).build();

        final Set<? extends EntityField<AuditedType, ?>> fieldsToChange = ImmutableSet.of(AuditedType.NAME,
                                                                                          AuditedType.DESC,
                                                                                          AuditedType.DESC2);

        final Stream<? extends EntityField<?, ?>> actualFieldsToFetch = INSTANCE.resolve(auditedFieldSet, fieldsToChange);

        assertThat(actualFieldsToFetch.collect(toSet()),
                   is(Collections.<EntityField<?, ?>>singleton(AuditedType.ID)));
    }

    @Test
    public void resolve_FieldSetHasIdOnly_FieldsToChangeIncludeIdAndOthers_ShouldReturnIdOnly() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID).build();

        final Set<? extends EntityField<AuditedType, ?>> fieldsToChange = ImmutableSet.of(AuditedType.ID,
                                                                                          AuditedType.NAME,
                                                                                          AuditedType.DESC,
                                                                                          AuditedType.DESC2);

        final Stream<? extends EntityField<?, ?>> actualFieldsToFetch = INSTANCE.resolve(auditedFieldSet, fieldsToChange);

        assertThat(actualFieldsToFetch.collect(toSet()),
                   is(Collections.<EntityField<?, ?>>singleton(AuditedType.ID)));
    }

    @Test
    public void resolve_FieldSetHasIdAndExternalMandatoryOnly_FieldsToChangeNotEmpty_ShouldReturnFieldSet() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID)
                .withExternalMandatoryFields(NotAuditedAncestorType.NAME,
                                             NotAuditedAncestorType.DESC)
                .build();

        final Set<? extends EntityField<AuditedType, ?>> fieldsToChange = singleton(AuditedType.NAME);

        final Set<? extends EntityField<?, ?>> expectedFieldsToFetch = auditedFieldSet.getAllFields().collect(toSet());

        final Stream<? extends EntityField<?, ?>> actualFieldsToFetch = INSTANCE.resolve(auditedFieldSet, fieldsToChange);

        assertThat(actualFieldsToFetch.collect(Collectors.<EntityField<?, ?>>toSet()), is(expectedFieldsToFetch));
    }

    @Test
    public void resolve_FieldSetHasIdAndSelfMandatoryOnly_FieldsToChangeAreDifferent_ShouldReturnFieldSet() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID)
                .withSelfMandatoryFields(AuditedType.NAME, AuditedType.DESC)
                .build();

        final Set<? extends EntityField<AuditedType, ?>> fieldsToChange = singleton(AuditedType.DESC2);

        final Set<? extends EntityField<?, ?>> expectedFieldsToFetch = auditedFieldSet.getAllFields().collect(toSet());

        final Stream<? extends EntityField<?, ?>> actualFieldsToFetch = INSTANCE.resolve(auditedFieldSet, fieldsToChange);

        assertThat(actualFieldsToFetch.collect(Collectors.<EntityField<?, ?>>toSet()), is(expectedFieldsToFetch));
    }

    @Test
    public void resolve_FieldSetHasIdAndSelfMandatoryOnly_FieldsToChangeAreTheSame_ShouldReturnFieldsToChange() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID)
                .withSelfMandatoryFields(AuditedType.NAME, AuditedType.DESC)
                .build();

        final Set<? extends EntityField<AuditedType, ?>> fieldsToChange = ImmutableSet.of(AuditedType.ID,
                                                                                          AuditedType.NAME,
                                                                                          AuditedType.DESC);

        final Stream<? extends EntityField<?, ?>> actualFieldsToFetch = INSTANCE.resolve(auditedFieldSet, fieldsToChange);

        assertThat(actualFieldsToFetch.collect(Collectors.<EntityField<?, ?>>toSet()), is(fieldsToChange));
    }

    @Test
    public void resolve_FieldSetHasIdAndSelfMandatoryOnly_FieldsToChangePartiallyIntersect_ShouldReturnFieldSet() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID)
                .withSelfMandatoryFields(AuditedType.NAME, AuditedType.DESC)
                .build();

        final Set<? extends EntityField<AuditedType, ?>> fieldsToChange = ImmutableSet.of(AuditedType.DESC,
                                                                                          AuditedType.DESC2);

        final Set<? extends EntityField<?, ?>> expectedFieldsToFetch = auditedFieldSet.getAllFields().collect(toSet());

        final Stream<? extends EntityField<?, ?>> actualFieldsToFetch = INSTANCE.resolve(auditedFieldSet, fieldsToChange);

        assertThat(actualFieldsToFetch.collect(Collectors.<EntityField<?, ?>>toSet()), is(expectedFieldsToFetch));
    }

    @Test
    public void resolve_FieldSetHasIdAndOnChangeOnly_FieldsToChangeAreDifferent_ShouldReturnIdOnly() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID)
                .withOnChangeFields(ImmutableSet.of(AuditedType.NAME,
                                                    AuditedType.DESC))
                .build();

        final Set<? extends EntityField<AuditedType, ?>> fieldsToChange = singleton(AuditedType.DESC2);

        final Set<? extends EntityField<?, ?>> expectedFieldsToFetch = singleton(AuditedType.ID);

        final Stream<? extends EntityField<?, ?>> actualFieldsToFetch = INSTANCE.resolve(auditedFieldSet, fieldsToChange);

        assertThat(actualFieldsToFetch.collect(Collectors.<EntityField<?, ?>>toSet()), is(expectedFieldsToFetch));
    }

    @Test
    public void resolve_FieldSetHasIdAndOnChangeOnly_FieldsToChangeAreSame_ShouldReturnFieldsToChange() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID)
                .withOnChangeFields(ImmutableSet.of(AuditedType.NAME,
                                                    AuditedType.DESC))
                .build();

        final Set<? extends EntityField<AuditedType, ?>> fieldsToChange = ImmutableSet.of(AuditedType.ID,
                                                                                          AuditedType.NAME,
                                                                                          AuditedType.DESC);

        final Set<? extends EntityField<?, ?>> expectedFieldsToFetch = auditedFieldSet.getAllFields().collect(toSet());

        final Stream<? extends EntityField<?, ?>> actualFieldsToFetch = INSTANCE.resolve(auditedFieldSet, fieldsToChange);

        assertThat(actualFieldsToFetch.collect(Collectors.<EntityField<?, ?>>toSet()), is(expectedFieldsToFetch));
    }

    @Test
    public void resolve_FieldSetHasIdAndOnChangeOnly_FieldsToChangeIncludedInOnChange_ShouldReturnIdAndFieldsToChange() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID)
                .withOnChangeFields(ImmutableSet.of(AuditedType.NAME,
                                                    AuditedType.DESC,
                                                    AuditedType.DESC2))
                .build();

        final Set<? extends EntityField<AuditedType, ?>> fieldsToChange = ImmutableSet.of(AuditedType.DESC,
                                                                                          AuditedType.DESC2);

        final Set<? extends EntityField<?, ?>> expectedFieldsToFetch = ImmutableSet.of(AuditedType.ID,
                                                                                       AuditedType.DESC,
                                                                                       AuditedType.DESC2);

        final Stream<? extends EntityField<?, ?>> actualFieldsToFetch = INSTANCE.resolve(auditedFieldSet, fieldsToChange);

        assertThat(actualFieldsToFetch.collect(Collectors.<EntityField<?, ?>>toSet()), is(expectedFieldsToFetch));
    }

    @Test
    public void resolve_FieldSetHasIdAndOnChangeOnly_FieldsToChangeContainOnChange_ShouldReturnFieldSet() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID)
                .withOnChangeFields(ImmutableSet.of(AuditedType.NAME,
                                                    AuditedType.DESC))
                .build();

        final Set<? extends EntityField<AuditedType, ?>> fieldsToChange = ImmutableSet.of(AuditedType.NAME,
                                                                                          AuditedType.DESC,
                                                                                          AuditedType.DESC2);

        final Set<? extends EntityField<?, ?>> expectedFieldsToFetch = auditedFieldSet.getAllFields().collect(toSet());

        final Stream<? extends EntityField<?, ?>> actualFieldsToFetch = INSTANCE.resolve(auditedFieldSet, fieldsToChange);

        assertThat(actualFieldsToFetch.collect(Collectors.<EntityField<?, ?>>toSet()), is(expectedFieldsToFetch));
    }

    @Test
    public void resolve_FieldSetHasIdAndOnChangeOnly_FieldsToChangePartiallyIntersectOnChange_ShouldReturnIdAndIntersection() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID)
                .withOnChangeFields(ImmutableSet.of(AuditedType.NAME,
                                                    AuditedType.DESC))
                .build();

        final Set<? extends EntityField<AuditedType, ?>> fieldsToChange = ImmutableSet.of(AuditedType.DESC,
                                                                                          AuditedType.DESC2);

        final Set<? extends EntityField<?, ?>> expectedFieldsToFetch = ImmutableSet.of(AuditedType.ID,
                                                                                       AuditedType.DESC);

        final Stream<? extends EntityField<?, ?>> actualFieldsToFetch = INSTANCE.resolve(auditedFieldSet, fieldsToChange);

        assertThat(actualFieldsToFetch.collect(Collectors.<EntityField<?, ?>>toSet()), is(expectedFieldsToFetch));
    }

    @Test
    public void resolve_FieldSetHasEverything_FieldsToChangePartiallyIntersectOnChange_ShouldReturnIdAndMandatoriesAndIntersection() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID)
                .withExternalMandatoryFields(ImmutableSet.of(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC))
                .withSelfMandatoryFields(singleton(AuditedType.NAME))
                .withOnChangeFields(ImmutableSet.of(AuditedType.DESC, AuditedType.DESC2))
                .build();

        final Set<? extends EntityField<AuditedType, ?>> fieldsToChange = singleton(AuditedType.DESC);

        final Set<? extends EntityField<?, ?>> expectedFieldsToFetch = ImmutableSet.of(AuditedType.ID,
                                                                                       NotAuditedAncestorType.NAME,
                                                                                       NotAuditedAncestorType.DESC,
                                                                                       AuditedType.NAME,
                                                                                       AuditedType.DESC);

        final Stream<? extends EntityField<?, ?>> actualFieldsToFetch = INSTANCE.resolve(auditedFieldSet, fieldsToChange);

        assertThat(actualFieldsToFetch.collect(Collectors.<EntityField<?, ?>>toSet()), is(expectedFieldsToFetch));
    }
}