package com.kenshoo.pl.entity.internal.audit;

import com.google.common.collect.ImmutableSet;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedAncestorType;
import org.junit.Test;

import java.util.Set;
import java.util.stream.Stream;

import static com.kenshoo.pl.entity.audit.AuditTrigger.*;
import static com.kenshoo.pl.entity.internal.audit.AuditedEntityType.builder;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertThat;

public class AuditedEntityTypeTest {

    @Test
    public void getName_Default() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID).build();

        assertThat(auditedEntityType.getName(), is(AuditedType.INSTANCE.getName()));
    }

    @Test
    public void setAndGetName() {
        final String name = "someName";
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID)
                .withName(name)
                .build();

        assertThat(auditedEntityType.getName(), is(name));
    }

    @Test
    public void getInternalFields_WhenHasOnCreateOrUpdate() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID)
                .withUnderlyingInternalFields(ON_CREATE_OR_UPDATE, AuditedType.NAME, AuditedType.DESC)
                .build();

        final Set<AuditedField<?, ?>> expectedInternalFields =
            Stream.<EntityField<?, ?>>of(AuditedType.NAME,
                                         AuditedType.DESC)
                  .map(AuditedField::new)
                  .collect(toUnmodifiableSet());

        assertThat(auditedEntityType.getInternalFields().collect(toSet()), is(expectedInternalFields));
    }

    @Test
    public void getInternalFields_WhenHasOnUpdate() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID)
                .withUnderlyingInternalFields(ON_UPDATE, AuditedType.NAME, AuditedType.DESC)
                .build();

        final Set<AuditedField<?, ?>> expectedInternalFields =
            Stream.<EntityField<?, ?>>of(AuditedType.NAME,
                                         AuditedType.DESC)
                  .map(AuditedField::new)
                  .collect(toUnmodifiableSet());

        assertThat(auditedEntityType.getInternalFields().collect(toSet()), is(expectedInternalFields));
    }

    @Test
    public void getInternalFields_WhenHasInternalMandatory() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID)
                .withUnderlyingInternalFields(ALWAYS, AuditedType.NAME, AuditedType.DESC)
                .build();

        final Set<AuditedField<?, ?>> expectedInternalFields =
            Stream.<EntityField<?, ?>>of(AuditedType.NAME,
                                         AuditedType.DESC)
                  .map(AuditedField::new)
                  .collect(toUnmodifiableSet());

        assertThat(auditedEntityType.getInternalFields().collect(toSet()), is(expectedInternalFields));
    }

    @Test
    public void getInternalFields_WhenHasOnCreateOrUpdateAndOnUpdate() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID)
                .withUnderlyingInternalFields(ON_CREATE_OR_UPDATE, AuditedType.NAME, AuditedType.DESC)
                .withUnderlyingInternalFields(ON_UPDATE, AuditedType.DESC2)
                .build();

        final Set<AuditedField<?, ?>> expectedInternalFields =
            Stream.<EntityField<?, ?>>of(AuditedType.NAME,
                                         AuditedType.DESC,
                                         AuditedType.DESC2)
                  .map(AuditedField::new)
                  .collect(toUnmodifiableSet());

        assertThat(auditedEntityType.getInternalFields().collect(toSet()), is(expectedInternalFields));
    }

    @Test
    public void getInternalFields_WhenHasOnCreateOrUpdateAndInternalMandatory() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID)
                .withUnderlyingInternalFields(ALWAYS, AuditedType.NAME)
                .withUnderlyingInternalFields(ON_CREATE_OR_UPDATE, AuditedType.DESC, AuditedType.DESC2)
                .build();

        final Set<AuditedField<?, ?>> expectedInternalFields =
            Stream.<EntityField<?, ?>>of(AuditedType.NAME,
                                         AuditedType.DESC,
                                         AuditedType.DESC2)
                  .map(AuditedField::new)
                  .collect(toUnmodifiableSet());

        assertThat(auditedEntityType.getInternalFields().collect(toSet()), is(expectedInternalFields));
    }

    @Test
    public void getInternalFields_WhenHasNone() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID).build();

        assertThat(auditedEntityType.getInternalFields().collect(toSet()), is(empty()));
    }

    @Test
    public void getUnderlyingOnChangeFields_WhenHasOnCreateOrUpdate() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID)
                .withUnderlyingInternalFields(ON_CREATE_OR_UPDATE, AuditedType.NAME, AuditedType.DESC)
                .build();

        final Set<EntityField<?, ?>> expectedOnChangeFields =
            ImmutableSet.of(AuditedType.NAME, AuditedType.DESC);

        assertThat(auditedEntityType.getUnderlyingOnChangeFields().collect(toSet()), is(expectedOnChangeFields));
    }

    @Test
    public void getUnderlyingOnChangeFields_WhenHasOnUpdate() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID)
                .withUnderlyingInternalFields(ON_UPDATE, AuditedType.NAME, AuditedType.DESC)
                .build();

        final Set<EntityField<?, ?>> expectedOnChangeFields =
            ImmutableSet.of(AuditedType.NAME, AuditedType.DESC);

        assertThat(auditedEntityType.getUnderlyingOnChangeFields().collect(toSet()), is(expectedOnChangeFields));
    }

    @Test
    public void getUnderlyingOnChangeFields_WhenHasExternalMandatory() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID)
                .withUnderlyingExternalFields(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC)
                .build();

        assertThat(auditedEntityType.getUnderlyingOnChangeFields().collect(toSet()), is(empty()));
    }

    @Test
    public void getUnderlyingOnChangeFields_WhenHasInternalMandatory() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID)
                .withUnderlyingInternalFields(ALWAYS, AuditedType.NAME, AuditedType.DESC)
                .build();

        assertThat(auditedEntityType.getUnderlyingOnChangeFields().collect(toSet()), is(empty()));
    }

    @Test
    public void getUnderlyingOnChangeFields_WhenHasOnCreateOrUpdateAndOnUpdate() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID)
                .withUnderlyingInternalFields(ON_CREATE_OR_UPDATE, AuditedType.NAME, AuditedType.DESC)
                .withUnderlyingInternalFields(ON_UPDATE, AuditedType.DESC2)
                .build();

        final Set<EntityField<?, ?>> expectedOnChangeFields =
            ImmutableSet.of(AuditedType.NAME,
                            AuditedType.DESC,
                            AuditedType.DESC2);

        assertThat(auditedEntityType.getUnderlyingOnChangeFields().collect(toSet()), is(expectedOnChangeFields));
    }

    @Test
    public void getUnderlyingOnChangeFields_WhenHasOnCreateOrUpdateAndInternalMandatory() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID)
                .withUnderlyingInternalFields(ALWAYS, AuditedType.NAME)
                .withUnderlyingInternalFields(ON_CREATE_OR_UPDATE, AuditedType.DESC, AuditedType.DESC2)
                .build();

        final Set<EntityField<?, ?>> expectedOnChangeFields =
            ImmutableSet.of(AuditedType.DESC, AuditedType.DESC2);

        assertThat(auditedEntityType.getUnderlyingOnChangeFields().collect(toSet()), is(expectedOnChangeFields));
    }

    @Test
    public void getUnderlyingOnChangeFields_WhenHasNone() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID).build();

        assertThat(auditedEntityType.getUnderlyingOnChangeFields().collect(toSet()), is(empty()));
    }

    @Test
    public void getUnderlyingMandatoryFields_WhenHasExternalMandatory() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID)
                .withUnderlyingExternalFields(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC)
                .build();

        final Set<EntityField<?, ?>> expectedFields =
            Set.of(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC);

        assertThat(auditedEntityType.getUnderlyingMandatoryFields().collect(toSet()), is(expectedFields));
    }

    @Test
    public void getUnderlyingMandatoryFields_WhenHasInternalMandatory() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID)
                .withUnderlyingInternalFields(ALWAYS, AuditedType.NAME, AuditedType.DESC)
                .build();

        final Set<EntityField<?, ?>> expectedFields =
            Set.of(AuditedType.NAME, AuditedType.DESC);

        assertThat(auditedEntityType.getUnderlyingMandatoryFields().collect(toSet()), is(expectedFields));
    }

    @Test
    public void getUnderlyingMandatoryFields_WhenHasExternalAndInternalMandatory() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID)
                .withUnderlyingExternalFields(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC)
                .withUnderlyingInternalFields(ALWAYS, AuditedType.NAME, AuditedType.DESC)
                .build();

        final Set<EntityField<?, ?>> expectedAllFields =
            Set.of(NotAuditedAncestorType.NAME,
                   NotAuditedAncestorType.DESC,
                   AuditedType.NAME,
                   AuditedType.DESC);

        assertThat(auditedEntityType.getUnderlyingMandatoryFields().collect(toSet()), is(expectedAllFields));
    }

    @Test
    public void getUnderlyingMandatoryFields_WhenHasNoMandatory() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID)
                .withUnderlyingInternalFields(ON_CREATE_OR_UPDATE, AuditedType.NAME, AuditedType.DESC)
                .build();

        assertThat(auditedEntityType.getUnderlyingMandatoryFields().collect(toSet()), is(empty()));
    }

    @Test
    public void getMandatoryFields_WhenHasExternalMandatory() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID)
                .withUnderlyingExternalFields(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC)
                .build();

        final Set<AuditedField<?, ?>> expectedAllFields =
            Stream.of(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC)
                  .map(AuditedField::new)
                  .collect(toUnmodifiableSet());

        assertThat(auditedEntityType.getMandatoryFields().collect(toSet()), is(expectedAllFields));
    }

    @Test
    public void getMandatoryFields_WhenHasInternalMandatory() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID)
                .withUnderlyingInternalFields(ALWAYS, AuditedType.NAME, AuditedType.DESC)
                .build();

        final Set<AuditedField<?, ?>> expectedAllFields =
            Stream.of(AuditedType.NAME, AuditedType.DESC)
                  .map(AuditedField::new)
                  .collect(toUnmodifiableSet());

        assertThat(auditedEntityType.getMandatoryFields().collect(toSet()), is(expectedAllFields));
    }

    @Test
    public void getMandatoryFields_WhenHasExternalAndInternalMandatory() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID)
                .withUnderlyingExternalFields(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC)
                .withUnderlyingInternalFields(ALWAYS, AuditedType.NAME, AuditedType.DESC)
                .build();

        final Set<AuditedField<?, ?>> expectedAllFields =
            Stream.<EntityField<?, ?>>of(NotAuditedAncestorType.NAME,
                                         NotAuditedAncestorType.DESC,
                                         AuditedType.NAME,
                                         AuditedType.DESC)
                  .map(AuditedField::new)
                  .collect(toUnmodifiableSet());

        assertThat(auditedEntityType.getMandatoryFields().collect(toSet()), is(expectedAllFields));
    }

    @Test
    public void getMandatoryFields_WhenHasNoMandatory() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID)
                .withUnderlyingInternalFields(ON_CREATE_OR_UPDATE, AuditedType.NAME, AuditedType.DESC)
                .build();

        assertThat(auditedEntityType.getMandatoryFields().collect(toSet()), is(empty()));
    }

    @Test
    public void hasInternalFields_WhenHasOnCreateOrUpdateFields_ShouldReturnTrue() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID)
                .withUnderlyingInternalFields(ON_CREATE_OR_UPDATE, AuditedType.NAME, AuditedType.DESC)
                .build();

        assertThat(auditedEntityType.hasInternalFields(), is(true));
    }

    @Test
    public void hasInternalFields_WhenHasOnUpdateFields_ShouldReturnTrue() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID)
                .withUnderlyingInternalFields(ON_UPDATE, AuditedType.NAME, AuditedType.DESC)
                .build();

        assertThat(auditedEntityType.hasInternalFields(), is(true));
    }

    @Test
    public void hasInternalFields_WhenHasInternalMandatoryFields_ShouldReturnTrue() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID)
                .withUnderlyingInternalFields(ALWAYS, AuditedType.NAME, AuditedType.DESC)
                .build();

        assertThat(auditedEntityType.hasInternalFields(), is(true));
    }

    @Test
    public void hasInternalFields_WhenHasIdOnly_ShouldReturnFalse() {
        final AuditedEntityType<AuditedType> auditedEntityType = builder(AuditedType.ID).build();

        assertThat(auditedEntityType.hasInternalFields(), is(false));
    }

    @Test
    public void hasInternalFields_WhenHasIdAndExternalMandatoryOnly_ShouldReturnFalse() {
        final AuditedEntityType<AuditedType> auditedEntityType =
            builder(AuditedType.ID)
                .withUnderlyingExternalFields(NotAuditedAncestorType.NAME)
                .build();

        assertThat(auditedEntityType.hasInternalFields(), is(false));
    }
}