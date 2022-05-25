package com.kenshoo.pl.entity.internal.audit;

import com.google.common.collect.ImmutableSet;
import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedAutoIncIdType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedAncestorType;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.kenshoo.pl.entity.audit.AuditTrigger.*;
import static com.kenshoo.pl.entity.internal.audit.AuditedEntityType.builder;
import static java.util.Collections.singleton;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AuditRequiredFieldsCalculatorTest {

    private static final ChangeOperation OPERATOR = ChangeOperation.UPDATE;

    @Test
    public void requiredFields_FieldSetHasIdOnly_FieldsToChangeAreDifferent_ShouldReturnIdOnly() {
        final AuditedEntityType<AuditedAutoIncIdType> auditedEntityType =
            builder(AuditedAutoIncIdType.ID).build();

        final Set<? extends EntityField<AuditedAutoIncIdType, ?>> fieldsToChange = ImmutableSet.of(AuditedAutoIncIdType.NAME,
                                                                                          AuditedAutoIncIdType.DESC,
                                                                                          AuditedAutoIncIdType.DESC2);

        final Stream<? extends EntityField<?, ?>> actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(toSet()),
                   is(Collections.<EntityField<?, ?>>singleton(AuditedAutoIncIdType.ID)));
    }

    @Test
    public void requiredFields_FieldSetHasIdOnly_FieldsToChangeIncludeIdAndOthers_ShouldReturnIdOnly() {
        final AuditedEntityType<AuditedAutoIncIdType> auditedEntityType =
            builder(AuditedAutoIncIdType.ID).build();

        final Set<? extends EntityField<AuditedAutoIncIdType, ?>> fieldsToChange = ImmutableSet.of(AuditedAutoIncIdType.ID,
                                                                                          AuditedAutoIncIdType.NAME,
                                                                                          AuditedAutoIncIdType.DESC,
                                                                                          AuditedAutoIncIdType.DESC2);

        final Stream<? extends EntityField<?, ?>> actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(toSet()),
                   is(Collections.<EntityField<?, ?>>singleton(AuditedAutoIncIdType.ID)));
    }

    @Test
    public void requiredFields_FieldSetHasIdAndExternalMandatoryOnly_FieldsToChangeNotEmpty_ShouldReturnFieldSet() {
        final AuditedEntityType<AuditedAutoIncIdType> auditedEntityType =
            builder(AuditedAutoIncIdType.ID)
                .withUnderlyingExternalFields(NotAuditedAncestorType.NAME,
                                              NotAuditedAncestorType.DESC)
                .build();

        final Set<? extends EntityField<AuditedAutoIncIdType, ?>> fieldsToChange = singleton(AuditedAutoIncIdType.NAME);

        final Set<? extends EntityField<?, ?>> expectedFieldsToFetch = toEntityFields(auditedEntityType);

        final Stream<? extends EntityField<?, ?>> actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toSet()), is(expectedFieldsToFetch));
    }

    @Test
    public void requiredFields_FieldSetHasIdAndSelfMandatoryOnly_FieldsToChangeAreDifferent_ShouldReturnFieldSet() {
        final AuditedEntityType<AuditedAutoIncIdType> auditedEntityType =
            builder(AuditedAutoIncIdType.ID)
                .withUnderlyingInternalFields(ALWAYS, AuditedAutoIncIdType.NAME, AuditedAutoIncIdType.DESC)
                .build();

        final Set<? extends EntityField<AuditedAutoIncIdType, ?>> fieldsToChange = singleton(AuditedAutoIncIdType.DESC2);

        final Set<? extends EntityField<?, ?>> expectedFieldsToFetch = toEntityFields(auditedEntityType);

        final Stream<? extends EntityField<?, ?>> actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toSet()), is(expectedFieldsToFetch));
    }

    @Test
    public void requiredFields_FieldSetHasIdAndSelfMandatoryOnly_FieldsToChangeAreTheSame_ShouldReturnFieldsToChange() {
        final AuditedEntityType<AuditedAutoIncIdType> auditedEntityType =
            builder(AuditedAutoIncIdType.ID)
                .withUnderlyingInternalFields(ALWAYS, AuditedAutoIncIdType.NAME, AuditedAutoIncIdType.DESC)
                .build();

        final Set<? extends EntityField<AuditedAutoIncIdType, ?>> fieldsToChange = ImmutableSet.of(AuditedAutoIncIdType.ID,
                                                                                          AuditedAutoIncIdType.NAME,
                                                                                          AuditedAutoIncIdType.DESC);

        final Stream<? extends EntityField<?, ?>> actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toSet()), is(fieldsToChange));
    }

    @Test
    public void requiredFields_FieldSetHasIdAndSelfMandatoryOnly_FieldsToChangePartiallyIntersect_ShouldReturnFieldSet() {
        final AuditedEntityType<AuditedAutoIncIdType> auditedEntityType =
            builder(AuditedAutoIncIdType.ID)
                .withUnderlyingInternalFields(ALWAYS, AuditedAutoIncIdType.NAME, AuditedAutoIncIdType.DESC)
                .build();

        final Set<? extends EntityField<AuditedAutoIncIdType, ?>> fieldsToChange = ImmutableSet.of(AuditedAutoIncIdType.DESC,
                                                                                          AuditedAutoIncIdType.DESC2);

        final Set<? extends EntityField<?, ?>> expectedFieldsToFetch = toEntityFields(auditedEntityType);

        final Stream<? extends EntityField<?, ?>> actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toSet()), is(expectedFieldsToFetch));
    }

    @Test
    public void requiredFields_FieldSetHasIdAndOnCreateOrUpdateOnly_FieldsToChangeAreDifferent_ShouldReturnIdOnly() {
        final AuditedEntityType<AuditedAutoIncIdType> auditedEntityType =
            builder(AuditedAutoIncIdType.ID)
                .withUnderlyingInternalFields(ON_CREATE_OR_UPDATE, AuditedAutoIncIdType.NAME, AuditedAutoIncIdType.DESC)
                .build();

        final Set<? extends EntityField<AuditedAutoIncIdType, ?>> fieldsToChange = singleton(AuditedAutoIncIdType.DESC2);

        final Set<? extends EntityField<?, ?>> expectedFieldsToFetch = singleton(AuditedAutoIncIdType.ID);

        final Stream<? extends EntityField<?, ?>> actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toSet()), is(expectedFieldsToFetch));
    }

    @Test
    public void requiredFields_FieldSetHasIdAndOnCreateOrUpdateOnly_FieldsToChangeAreSame_ShouldReturnFieldsToChange() {
        final AuditedEntityType<AuditedAutoIncIdType> auditedEntityType =
            builder(AuditedAutoIncIdType.ID)
                .withUnderlyingInternalFields(ON_CREATE_OR_UPDATE, AuditedAutoIncIdType.NAME, AuditedAutoIncIdType.DESC)
                .build();

        final Set<? extends EntityField<AuditedAutoIncIdType, ?>> fieldsToChange = ImmutableSet.of(AuditedAutoIncIdType.ID,
                                                                                          AuditedAutoIncIdType.NAME,
                                                                                          AuditedAutoIncIdType.DESC);

        final Set<? extends EntityField<?, ?>> expectedFieldsToFetch = toEntityFields(auditedEntityType);

        final Stream<? extends EntityField<?, ?>> actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toSet()), is(expectedFieldsToFetch));
    }

    @Test
    public void requiredFields_FieldSetHasIdAndOnCreateOrUpdateOnly_FieldsToChangeIncludedInOnCreateOrUpdate_ShouldReturnIdAndFieldsToChange() {
        final AuditedEntityType<AuditedAutoIncIdType> auditedEntityType =
            builder(AuditedAutoIncIdType.ID)
                .withUnderlyingInternalFields(ON_CREATE_OR_UPDATE,
                                              AuditedAutoIncIdType.NAME, AuditedAutoIncIdType.DESC, AuditedAutoIncIdType.DESC2)
                .build();

        final Set<? extends EntityField<AuditedAutoIncIdType, ?>> fieldsToChange = ImmutableSet.of(AuditedAutoIncIdType.DESC,
                                                                                          AuditedAutoIncIdType.DESC2);

        final Set<? extends EntityField<?, ?>> expectedFieldsToFetch = ImmutableSet.of(AuditedAutoIncIdType.ID,
                                                                                       AuditedAutoIncIdType.DESC,
                                                                                       AuditedAutoIncIdType.DESC2);

        final Stream<? extends EntityField<?, ?>> actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toSet()), is(expectedFieldsToFetch));
    }

    @Test
    public void requiredFields_FieldSetHasIdAndOnCreateOrUpdateOnly_FieldsToChangeContainOnCreateOrUpdate_ShouldReturnFieldSet() {
        final AuditedEntityType<AuditedAutoIncIdType> auditedEntityType =
            builder(AuditedAutoIncIdType.ID)
                .withUnderlyingInternalFields(ON_CREATE_OR_UPDATE,
                                              AuditedAutoIncIdType.NAME, AuditedAutoIncIdType.DESC)
                .build();

        final Set<? extends EntityField<AuditedAutoIncIdType, ?>> fieldsToChange = ImmutableSet.of(AuditedAutoIncIdType.NAME,
                                                                                          AuditedAutoIncIdType.DESC,
                                                                                          AuditedAutoIncIdType.DESC2);

        final Set<? extends EntityField<?, ?>> expectedFieldsToFetch = toEntityFields(auditedEntityType);

        final Stream<? extends EntityField<?, ?>> actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toSet()), is(expectedFieldsToFetch));
    }

    @Test
    public void requiredFields_FieldSetHasIdAndOnCreateOrUpdateOnly_FieldsToChangePartiallyIntersectOnCreateOrUpdate_ShouldReturnIdAndIntersection() {
        final AuditedEntityType<AuditedAutoIncIdType> auditedEntityType =
            builder(AuditedAutoIncIdType.ID)
                .withUnderlyingInternalFields(ON_CREATE_OR_UPDATE, AuditedAutoIncIdType.NAME, AuditedAutoIncIdType.DESC)
                .build();

        final Set<? extends EntityField<AuditedAutoIncIdType, ?>> fieldsToChange = ImmutableSet.of(AuditedAutoIncIdType.DESC,
                                                                                          AuditedAutoIncIdType.DESC2);

        final Set<? extends EntityField<?, ?>> expectedFieldsToFetch = ImmutableSet.of(AuditedAutoIncIdType.ID,
                                                                                       AuditedAutoIncIdType.DESC);

        final Stream<? extends EntityField<?, ?>> actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toSet()), is(expectedFieldsToFetch));
    }

    @Test
    public void requiredFields_FieldSetHasIdAndOnUpdateOnly_FieldsToChangeAreDifferent_ShouldReturnIdOnly() {
        final AuditedEntityType<AuditedAutoIncIdType> auditedEntityType =
            builder(AuditedAutoIncIdType.ID)
                .withUnderlyingInternalFields(ON_UPDATE, AuditedAutoIncIdType.NAME, AuditedAutoIncIdType.DESC)
                .build();

        final Set<? extends EntityField<AuditedAutoIncIdType, ?>> fieldsToChange = singleton(AuditedAutoIncIdType.DESC2);

        final Set<? extends EntityField<?, ?>> expectedFieldsToFetch = singleton(AuditedAutoIncIdType.ID);

        final Stream<? extends EntityField<?, ?>> actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toSet()), is(expectedFieldsToFetch));
    }

    @Test
    public void requiredFields_FieldSetHasIdAndOnUpdateOnly_FieldsToChangeAreSame_ShouldReturnFieldsToChange() {
        final AuditedEntityType<AuditedAutoIncIdType> auditedEntityType =
            builder(AuditedAutoIncIdType.ID)
                .withUnderlyingInternalFields(ON_UPDATE, AuditedAutoIncIdType.NAME, AuditedAutoIncIdType.DESC)
                .build();

        final Set<? extends EntityField<AuditedAutoIncIdType, ?>> fieldsToChange = ImmutableSet.of(AuditedAutoIncIdType.ID,
                                                                                          AuditedAutoIncIdType.NAME,
                                                                                          AuditedAutoIncIdType.DESC);

        final Set<? extends EntityField<?, ?>> expectedFieldsToFetch = toEntityFields(auditedEntityType);

        final Stream<? extends EntityField<?, ?>> actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toSet()), is(expectedFieldsToFetch));
    }

    @Test
    public void requiredFields_FieldSetHasIdAndOnUpdateOnly_FieldsToChangeIncludedInOnCreateOrUpdate_ShouldReturnIdAndFieldsToChange() {
        final AuditedEntityType<AuditedAutoIncIdType> auditedEntityType =
            builder(AuditedAutoIncIdType.ID)
                .withUnderlyingInternalFields(ON_UPDATE,
                                              AuditedAutoIncIdType.NAME, AuditedAutoIncIdType.DESC, AuditedAutoIncIdType.DESC2)
                .build();

        final Set<? extends EntityField<AuditedAutoIncIdType, ?>> fieldsToChange = ImmutableSet.of(AuditedAutoIncIdType.DESC,
                                                                                          AuditedAutoIncIdType.DESC2);

        final Set<? extends EntityField<?, ?>> expectedFieldsToFetch = ImmutableSet.of(AuditedAutoIncIdType.ID,
                                                                                       AuditedAutoIncIdType.DESC,
                                                                                       AuditedAutoIncIdType.DESC2);

        final Stream<? extends EntityField<?, ?>> actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toSet()), is(expectedFieldsToFetch));
    }

    @Test
    public void requiredFields_FieldSetHasIdAndOnUpdateOnly_FieldsToChangeContainOnCreateOrUpdate_ShouldReturnFieldSet() {
        final AuditedEntityType<AuditedAutoIncIdType> auditedEntityType =
            builder(AuditedAutoIncIdType.ID)
                .withUnderlyingInternalFields(ON_UPDATE, AuditedAutoIncIdType.NAME, AuditedAutoIncIdType.DESC)
                .build();

        final Set<? extends EntityField<AuditedAutoIncIdType, ?>> fieldsToChange = ImmutableSet.of(AuditedAutoIncIdType.NAME,
                                                                                          AuditedAutoIncIdType.DESC,
                                                                                          AuditedAutoIncIdType.DESC2);

        final Set<? extends EntityField<?, ?>> expectedFieldsToFetch = toEntityFields(auditedEntityType);

        final Stream<? extends EntityField<?, ?>> actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toSet()), is(expectedFieldsToFetch));
    }

    @Test
    public void requiredFields_FieldSetHasIdAndOnUpdateOnly_FieldsToChangePartiallyIntersectOnCreateOrUpdate_ShouldReturnIdAndIntersection() {
        final AuditedEntityType<AuditedAutoIncIdType> auditedEntityType =
            builder(AuditedAutoIncIdType.ID)
                .withUnderlyingInternalFields(ON_UPDATE, AuditedAutoIncIdType.NAME, AuditedAutoIncIdType.DESC)
                .build();

        final Set<? extends EntityField<AuditedAutoIncIdType, ?>> fieldsToChange = ImmutableSet.of(AuditedAutoIncIdType.DESC,
                                                                                          AuditedAutoIncIdType.DESC2);

        final Set<? extends EntityField<?, ?>> expectedFieldsToFetch = ImmutableSet.of(AuditedAutoIncIdType.ID,
                                                                                       AuditedAutoIncIdType.DESC);

        final Stream<? extends EntityField<?, ?>> actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toSet()), is(expectedFieldsToFetch));
    }

    @Test
    public void requiredFields_FieldSetHasEverything_FieldsToChangePartiallyIntersectOnCreateOrUpdate_ShouldReturnIdAndMandatoriesAndIntersection() {
        final AuditedEntityType<AuditedAutoIncIdType> auditedEntityType =
            builder(AuditedAutoIncIdType.ID)
                .withUnderlyingExternalFields(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC)
                .withUnderlyingInternalFields(ALWAYS, AuditedAutoIncIdType.NAME)
                .withUnderlyingInternalFields(ON_CREATE_OR_UPDATE, AuditedAutoIncIdType.DESC)
                .withUnderlyingInternalFields(ON_UPDATE, AuditedAutoIncIdType.DESC2)
                .build();

        final Set<? extends EntityField<AuditedAutoIncIdType, ?>> fieldsToChange = singleton(AuditedAutoIncIdType.DESC);

        final Set<? extends EntityField<?, ?>> expectedFieldsToFetch = ImmutableSet.of(AuditedAutoIncIdType.ID,
                                                                                       NotAuditedAncestorType.NAME,
                                                                                       NotAuditedAncestorType.DESC,
                                                                                       AuditedAutoIncIdType.NAME,
                                                                                       AuditedAutoIncIdType.DESC);

        final Stream<? extends EntityField<?, ?>> actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toSet()), is(expectedFieldsToFetch));
    }

    @Test
    public void requiredFields_FieldSetHasEverything_FieldsToChangePartiallyIntersectOnUpdate_ShouldReturnIdAndMandatoriesAndIntersection() {
        final AuditedEntityType<AuditedAutoIncIdType> auditedEntityType =
            builder(AuditedAutoIncIdType.ID)
                .withUnderlyingExternalFields(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC)
                .withUnderlyingInternalFields(ALWAYS, AuditedAutoIncIdType.NAME)
                .withUnderlyingInternalFields(ON_CREATE_OR_UPDATE, AuditedAutoIncIdType.DESC)
                .withUnderlyingInternalFields(ON_UPDATE, AuditedAutoIncIdType.DESC2)
                .build();

        final Set<? extends EntityField<AuditedAutoIncIdType, ?>> fieldsToChange = singleton(AuditedAutoIncIdType.DESC);

        final Set<? extends EntityField<?, ?>> expectedFieldsToFetch = ImmutableSet.of(AuditedAutoIncIdType.ID,
                                                                                       NotAuditedAncestorType.NAME,
                                                                                       NotAuditedAncestorType.DESC,
                                                                                       AuditedAutoIncIdType.NAME,
                                                                                       AuditedAutoIncIdType.DESC);

        final Stream<? extends EntityField<?, ?>> actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toSet()), is(expectedFieldsToFetch));
    }

    private Stream<? extends EntityField<?, ?>> calculate(final AuditedEntityType<AuditedAutoIncIdType> auditedEntityType,
                                                          Set<? extends EntityField<AuditedAutoIncIdType, ?>> fieldsToChange) {
        return new AuditRequiredFieldsCalculator<>(auditedEntityType).requiredFields(fieldsToChange, OPERATOR);
    }

    private Set<? extends EntityField<?, ?>> toEntityFields(final AuditedEntityType<?> auditedEntityType) {
        return Stream.<Stream<EntityField<?, ?>>>of(
            Stream.of(auditedEntityType.getIdField()),
            auditedEntityType.getExternalFields().stream().map(AuditedField::getField),
            auditedEntityType.getInternalFields().map(AuditedField::getField))
                     .flatMap(identity())
                     .collect(toUnmodifiableSet());
    }
}