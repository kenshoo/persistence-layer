package com.kenshoo.pl.entity.internal.audit;

import com.google.common.collect.ImmutableSet;
import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedAncestorType;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.kenshoo.pl.entity.audit.AuditTrigger.*;
import static com.kenshoo.pl.entity.internal.audit.AuditedEntityType.builder;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AuditRequiredFieldsCalculatorTest {
    
    private static final ChangeOperation OPERATOR = ChangeOperation.UPDATE;

    @Test
    public void requiredFields_FieldSetHasIdOnly_FieldsToChangeAreDifferent_ShouldReturnIdOnly() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID).build();

        final Set<? extends EntityField<AuditedType, ?>> fieldsToChange = ImmutableSet.of(AuditedType.NAME,
                                                                                          AuditedType.DESC,
                                                                                          AuditedType.DESC2);

        final Stream<? extends EntityField<?, ?>> actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(toSet()),
                   is(Collections.<EntityField<?, ?>>singleton(AuditedType.ID)));
    }

    @Test
    public void requiredFields_FieldSetHasIdOnly_FieldsToChangeIncludeIdAndOthers_ShouldReturnIdOnly() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID).build();

        final Set<? extends EntityField<AuditedType, ?>> fieldsToChange = ImmutableSet.of(AuditedType.ID,
                                                                                          AuditedType.NAME,
                                                                                          AuditedType.DESC,
                                                                                          AuditedType.DESC2);

        final Stream<? extends EntityField<?, ?>> actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(toSet()),
                   is(Collections.<EntityField<?, ?>>singleton(AuditedType.ID)));
    }

    @Test
    public void requiredFields_FieldSetHasIdAndExternalMandatoryOnly_FieldsToChangeNotEmpty_ShouldReturnFieldSet() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID)
                .withExternalFields(NotAuditedAncestorType.NAME,
                                    NotAuditedAncestorType.DESC)
                .build();

        final Set<? extends EntityField<AuditedType, ?>> fieldsToChange = singleton(AuditedType.NAME);

        final Set<? extends EntityField<?, ?>> expectedFieldsToFetch = auditedEntityType.getAllFields().collect(toSet());

        final Stream<? extends EntityField<?, ?>> actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toSet()), is(expectedFieldsToFetch));
    }

    @Test
    public void requiredFields_FieldSetHasIdAndSelfMandatoryOnly_FieldsToChangeAreDifferent_ShouldReturnFieldSet() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID)
                .withInternalFields(ALWAYS, AuditedType.NAME, AuditedType.DESC)
                .build();

        final Set<? extends EntityField<AuditedType, ?>> fieldsToChange = singleton(AuditedType.DESC2);

        final Set<? extends EntityField<?, ?>> expectedFieldsToFetch = auditedEntityType.getAllFields().collect(toSet());

        final Stream<? extends EntityField<?, ?>> actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toSet()), is(expectedFieldsToFetch));
    }

    @Test
    public void requiredFields_FieldSetHasIdAndSelfMandatoryOnly_FieldsToChangeAreTheSame_ShouldReturnFieldsToChange() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID)
                .withInternalFields(ALWAYS, AuditedType.NAME, AuditedType.DESC)
                .build();

        final Set<? extends EntityField<AuditedType, ?>> fieldsToChange = ImmutableSet.of(AuditedType.ID,
                                                                                          AuditedType.NAME,
                                                                                          AuditedType.DESC);

        final Stream<? extends EntityField<?, ?>> actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toSet()), is(fieldsToChange));
    }

    @Test
    public void requiredFields_FieldSetHasIdAndSelfMandatoryOnly_FieldsToChangePartiallyIntersect_ShouldReturnFieldSet() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID)
                .withInternalFields(ALWAYS, AuditedType.NAME, AuditedType.DESC)
                .build();

        final Set<? extends EntityField<AuditedType, ?>> fieldsToChange = ImmutableSet.of(AuditedType.DESC,
                                                                                          AuditedType.DESC2);

        final Set<? extends EntityField<?, ?>> expectedFieldsToFetch = auditedEntityType.getAllFields().collect(toSet());

        final Stream<? extends EntityField<?, ?>> actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toSet()), is(expectedFieldsToFetch));
    }

    @Test
    public void requiredFields_FieldSetHasIdAndOnCreateOrUpdateOnly_FieldsToChangeAreDifferent_ShouldReturnIdOnly() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID)
                .withInternalFields(ON_CREATE_OR_UPDATE, AuditedType.NAME, AuditedType.DESC)
                .build();

        final Set<? extends EntityField<AuditedType, ?>> fieldsToChange = singleton(AuditedType.DESC2);

        final Set<? extends EntityField<?, ?>> expectedFieldsToFetch = singleton(AuditedType.ID);

        final Stream<? extends EntityField<?, ?>> actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toSet()), is(expectedFieldsToFetch));
    }

    @Test
    public void requiredFields_FieldSetHasIdAndOnCreateOrUpdateOnly_FieldsToChangeAreSame_ShouldReturnFieldsToChange() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID)
                .withInternalFields(ON_CREATE_OR_UPDATE, AuditedType.NAME, AuditedType.DESC)
                .build();

        final Set<? extends EntityField<AuditedType, ?>> fieldsToChange = ImmutableSet.of(AuditedType.ID,
                                                                                          AuditedType.NAME,
                                                                                          AuditedType.DESC);

        final Set<? extends EntityField<?, ?>> expectedFieldsToFetch = auditedEntityType.getAllFields().collect(toSet());

        final Stream<? extends EntityField<?, ?>> actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toSet()), is(expectedFieldsToFetch));
    }

    @Test
    public void requiredFields_FieldSetHasIdAndOnCreateOrUpdateOnly_FieldsToChangeIncludedInOnCreateOrUpdate_ShouldReturnIdAndFieldsToChange() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID)
                .withInternalFields(ON_CREATE_OR_UPDATE,
                                    AuditedType.NAME, AuditedType.DESC, AuditedType.DESC2)
                .build();

        final Set<? extends EntityField<AuditedType, ?>> fieldsToChange = ImmutableSet.of(AuditedType.DESC,
                                                                                          AuditedType.DESC2);

        final Set<? extends EntityField<?, ?>> expectedFieldsToFetch = ImmutableSet.of(AuditedType.ID,
                                                                                       AuditedType.DESC,
                                                                                       AuditedType.DESC2);

        final Stream<? extends EntityField<?, ?>> actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toSet()), is(expectedFieldsToFetch));
    }

    @Test
    public void requiredFields_FieldSetHasIdAndOnCreateOrUpdateOnly_FieldsToChangeContainOnCreateOrUpdate_ShouldReturnFieldSet() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID)
                .withInternalFields(ON_CREATE_OR_UPDATE,
                                    AuditedType.NAME, AuditedType.DESC)
                .build();

        final Set<? extends EntityField<AuditedType, ?>> fieldsToChange = ImmutableSet.of(AuditedType.NAME,
                                                                                          AuditedType.DESC,
                                                                                          AuditedType.DESC2);

        final Set<? extends EntityField<?, ?>> expectedFieldsToFetch = auditedEntityType.getAllFields().collect(toSet());

        final Stream<? extends EntityField<?, ?>> actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toSet()), is(expectedFieldsToFetch));
    }

    @Test
    public void requiredFields_FieldSetHasIdAndOnCreateOrUpdateOnly_FieldsToChangePartiallyIntersectOnCreateOrUpdate_ShouldReturnIdAndIntersection() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID)
                .withInternalFields(ON_CREATE_OR_UPDATE, AuditedType.NAME, AuditedType.DESC)
                .build();

        final Set<? extends EntityField<AuditedType, ?>> fieldsToChange = ImmutableSet.of(AuditedType.DESC,
                                                                                          AuditedType.DESC2);

        final Set<? extends EntityField<?, ?>> expectedFieldsToFetch = ImmutableSet.of(AuditedType.ID,
                                                                                       AuditedType.DESC);

        final Stream<? extends EntityField<?, ?>> actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toSet()), is(expectedFieldsToFetch));
    }

    @Test
    public void requiredFields_FieldSetHasIdAndOnUpdateOnly_FieldsToChangeAreDifferent_ShouldReturnIdOnly() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID)
                .withInternalFields(ON_UPDATE, AuditedType.NAME, AuditedType.DESC)
                .build();

        final Set<? extends EntityField<AuditedType, ?>> fieldsToChange = singleton(AuditedType.DESC2);

        final Set<? extends EntityField<?, ?>> expectedFieldsToFetch = singleton(AuditedType.ID);

        final Stream<? extends EntityField<?, ?>> actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toSet()), is(expectedFieldsToFetch));
    }

    @Test
    public void requiredFields_FieldSetHasIdAndOnUpdateOnly_FieldsToChangeAreSame_ShouldReturnFieldsToChange() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID)
                .withInternalFields(ON_UPDATE, AuditedType.NAME, AuditedType.DESC)
                .build();

        final Set<? extends EntityField<AuditedType, ?>> fieldsToChange = ImmutableSet.of(AuditedType.ID,
                                                                                          AuditedType.NAME,
                                                                                          AuditedType.DESC);

        final Set<? extends EntityField<?, ?>> expectedFieldsToFetch = auditedEntityType.getAllFields().collect(toSet());

        final Stream<? extends EntityField<?, ?>> actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toSet()), is(expectedFieldsToFetch));
    }

    @Test
    public void requiredFields_FieldSetHasIdAndOnUpdateOnly_FieldsToChangeIncludedInOnCreateOrUpdate_ShouldReturnIdAndFieldsToChange() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID)
                .withInternalFields(ON_UPDATE,
                                    AuditedType.NAME, AuditedType.DESC, AuditedType.DESC2)
                .build();

        final Set<? extends EntityField<AuditedType, ?>> fieldsToChange = ImmutableSet.of(AuditedType.DESC,
                                                                                          AuditedType.DESC2);

        final Set<? extends EntityField<?, ?>> expectedFieldsToFetch = ImmutableSet.of(AuditedType.ID,
                                                                                       AuditedType.DESC,
                                                                                       AuditedType.DESC2);

        final Stream<? extends EntityField<?, ?>> actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toSet()), is(expectedFieldsToFetch));
    }

    @Test
    public void requiredFields_FieldSetHasIdAndOnUpdateOnly_FieldsToChangeContainOnCreateOrUpdate_ShouldReturnFieldSet() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID)
                .withInternalFields(ON_UPDATE, AuditedType.NAME, AuditedType.DESC)
                .build();

        final Set<? extends EntityField<AuditedType, ?>> fieldsToChange = ImmutableSet.of(AuditedType.NAME,
                                                                                          AuditedType.DESC,
                                                                                          AuditedType.DESC2);

        final Set<? extends EntityField<?, ?>> expectedFieldsToFetch = auditedEntityType.getAllFields().collect(toSet());

        final Stream<? extends EntityField<?, ?>> actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toSet()), is(expectedFieldsToFetch));
    }

    @Test
    public void requiredFields_FieldSetHasIdAndOnUpdateOnly_FieldsToChangePartiallyIntersectOnCreateOrUpdate_ShouldReturnIdAndIntersection() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID)
                .withInternalFields(ON_UPDATE, AuditedType.NAME, AuditedType.DESC)
                .build();

        final Set<? extends EntityField<AuditedType, ?>> fieldsToChange = ImmutableSet.of(AuditedType.DESC,
                                                                                          AuditedType.DESC2);

        final Set<? extends EntityField<?, ?>> expectedFieldsToFetch = ImmutableSet.of(AuditedType.ID,
                                                                                       AuditedType.DESC);

        final Stream<? extends EntityField<?, ?>> actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toSet()), is(expectedFieldsToFetch));
    }

    @Test
    public void requiredFields_FieldSetHasEverything_FieldsToChangePartiallyIntersectOnCreateOrUpdate_ShouldReturnIdAndMandatoriesAndIntersection() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID)
                .withExternalFields(ImmutableSet.of(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC))
                .withInternalFields(ALWAYS, singleton(AuditedType.NAME))
                .withInternalFields(ON_CREATE_OR_UPDATE, ImmutableSet.of(AuditedType.DESC))
                .withInternalFields(ON_UPDATE, ImmutableSet.of(AuditedType.DESC2))
                .build();

        final Set<? extends EntityField<AuditedType, ?>> fieldsToChange = singleton(AuditedType.DESC);

        final Set<? extends EntityField<?, ?>> expectedFieldsToFetch = ImmutableSet.of(AuditedType.ID,
                                                                                       NotAuditedAncestorType.NAME,
                                                                                       NotAuditedAncestorType.DESC,
                                                                                       AuditedType.NAME,
                                                                                       AuditedType.DESC);

        final Stream<? extends EntityField<?, ?>> actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toSet()), is(expectedFieldsToFetch));
    }

    @Test
    public void requiredFields_FieldSetHasEverything_FieldsToChangePartiallyIntersectOnUpdate_ShouldReturnIdAndMandatoriesAndIntersection() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID)
                .withExternalFields(ImmutableSet.of(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC))
                .withInternalFields(ALWAYS, singleton(AuditedType.NAME))
                .withInternalFields(ON_CREATE_OR_UPDATE, ImmutableSet.of(AuditedType.DESC))
                .withInternalFields(ON_UPDATE, ImmutableSet.of(AuditedType.DESC2))
                .build();

        final Set<? extends EntityField<AuditedType, ?>> fieldsToChange = singleton(AuditedType.DESC);

        final Set<? extends EntityField<?, ?>> expectedFieldsToFetch = ImmutableSet.of(AuditedType.ID,
                                                                                       NotAuditedAncestorType.NAME,
                                                                                       NotAuditedAncestorType.DESC,
                                                                                       AuditedType.NAME,
                                                                                       AuditedType.DESC);

        final Stream<? extends EntityField<?, ?>> actualRequiredFields = calculate(auditedEntityType, fieldsToChange);

        assertThat(actualRequiredFields.collect(Collectors.<EntityField<?, ?>>toSet()), is(expectedFieldsToFetch));
    }
    
    private Stream<? extends EntityField<?, ?>> calculate(final AuditedEntityType<AuditedType> auditedEntityType,
                                                          Set<? extends EntityField<AuditedType, ?>> fieldsToChange) {
        return new AuditRequiredFieldsCalculator<>(auditedEntityType).requiredFields(fieldsToChange, OPERATOR);
    }
}