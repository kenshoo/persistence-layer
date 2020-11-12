package com.kenshoo.pl.entity.internal.audit;

import com.google.common.collect.ImmutableSet;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedAncestorType;
import org.junit.Test;

import java.util.Set;

import static com.kenshoo.pl.entity.audit.AuditTrigger.*;
import static com.kenshoo.pl.entity.internal.audit.AuditedFieldSet.builder;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertThat;

public class AuditedFieldSetTest {

    @Test
    public void getAllFields_IdOnly() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID).build();

        final Set<EntityField<?, ?>> expectedAllFields = singleton(AuditedType.ID);

        assertThat(auditedFieldSet.getAllFields().collect(toSet()), is(expectedAllFields));
    }

    @Test
    public void getAllFields_IdAndExternalMandatory() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID)
                .withExternalFields(ImmutableSet.of(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC))
                .build();

        final Set<EntityField<?, ?>> expectedAllFields =
            ImmutableSet.of(AuditedType.ID,
                            NotAuditedAncestorType.NAME,
                            NotAuditedAncestorType.DESC);

        assertThat(auditedFieldSet.getAllFields().collect(toSet()), is(expectedAllFields));
    }

    @Test
    public void getAllFields_IdAndInternalMandatory() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID)
                .withInternalFields(ALWAYS, AuditedType.NAME, AuditedType.DESC)
                .build();

        final Set<EntityField<?, ?>> expectedAllFields =
            ImmutableSet.of(AuditedType.ID,
                            AuditedType.NAME,
                            AuditedType.DESC);

        assertThat(auditedFieldSet.getAllFields().collect(toSet()), is(expectedAllFields));
    }

    @Test
    public void getAllFields_IdAndOnCreateOrUpdate() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID)
                .withInternalFields(ON_CREATE_OR_UPDATE, AuditedType.NAME, AuditedType.DESC)
                .build();

        final Set<EntityField<?, ?>> expectedAllFields =
            ImmutableSet.of(AuditedType.ID,
                            AuditedType.NAME,
                            AuditedType.DESC);

        assertThat(auditedFieldSet.getAllFields().collect(toSet()), is(expectedAllFields));
    }

    @Test
    public void getAllFields_IdAndOnUpdate() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID)
                .withInternalFields(ON_UPDATE, AuditedType.NAME, AuditedType.DESC)
                .build();

        final Set<EntityField<?, ?>> expectedAllFields =
            ImmutableSet.of(AuditedType.ID,
                            AuditedType.NAME,
                            AuditedType.DESC);

        assertThat(auditedFieldSet.getAllFields().collect(toSet()), is(expectedAllFields));
    }

    @Test
    public void getAllFields_AllTypes() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID)
                .withExternalFields(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC)
                .withInternalFields(ALWAYS, AuditedType.NAME)
                .withInternalFields(ON_CREATE_OR_UPDATE, AuditedType.DESC)
                .withInternalFields(ON_UPDATE, AuditedType.DESC2)
                .build();

        final Set<EntityField<?, ?>> expectedAllFields =
            ImmutableSet.of(AuditedType.ID,
                            NotAuditedAncestorType.NAME,
                            NotAuditedAncestorType.DESC,
                            AuditedType.NAME,
                            AuditedType.DESC,
                            AuditedType.DESC2);

        assertThat(auditedFieldSet.getAllFields().collect(toSet()), is(expectedAllFields));
    }

    @Test
    public void getAllInternalFields_WhenHasOnCreateOrUpdate() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID)
                .withInternalFields(ON_CREATE_OR_UPDATE, AuditedType.NAME, AuditedType.DESC)
                .build();

        final Set<EntityField<?, ?>> expectedAllFields =
            ImmutableSet.of(AuditedType.NAME,
                            AuditedType.DESC);

        assertThat(auditedFieldSet.getInternalFields().collect(toSet()), is(expectedAllFields));
    }

    @Test
    public void getAllInternalFields_WhenHasOnUpdate() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID)
                .withInternalFields(ON_UPDATE, AuditedType.NAME, AuditedType.DESC)
                .build();

        final Set<EntityField<?, ?>> expectedAllFields =
            ImmutableSet.of(AuditedType.NAME,
                            AuditedType.DESC);

        assertThat(auditedFieldSet.getInternalFields().collect(toSet()), is(expectedAllFields));
    }

    @Test
    public void getAllInternalFields_WhenHasInternalMandatory() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID)
                .withInternalFields(ALWAYS, AuditedType.NAME, AuditedType.DESC)
                .build();

        final Set<EntityField<?, ?>> expectedAllFields =
            ImmutableSet.of(AuditedType.NAME,
                            AuditedType.DESC);

        assertThat(auditedFieldSet.getInternalFields().collect(toSet()), is(expectedAllFields));
    }

    @Test
    public void getAllInternalFields_WhenHasOnCreateOrUpdateAndOnUpdate() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID)
                .withInternalFields(ON_CREATE_OR_UPDATE, AuditedType.NAME, AuditedType.DESC)
                .withInternalFields(ON_UPDATE, AuditedType.DESC2)
                .build();

        final Set<EntityField<?, ?>> expectedAllFields =
            ImmutableSet.of(AuditedType.NAME,
                            AuditedType.DESC,
                            AuditedType.DESC2);

        assertThat(auditedFieldSet.getInternalFields().collect(toSet()), is(expectedAllFields));
    }

    @Test
    public void getAllInternalFields_WhenHasOnCreateOrUpdateAndInternalMandatory() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID)
                .withInternalFields(ALWAYS, AuditedType.NAME)
                .withInternalFields(ON_CREATE_OR_UPDATE, AuditedType.DESC, AuditedType.DESC2)
                .build();

        final Set<EntityField<?, ?>> expectedAllFields =
            ImmutableSet.of(AuditedType.NAME,
                            AuditedType.DESC,
                            AuditedType.DESC2);

        assertThat(auditedFieldSet.getInternalFields().collect(toSet()), is(expectedAllFields));
    }

    @Test
    public void getAllInternalFields_WhenHasNone() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID).build();

        assertThat(auditedFieldSet.getInternalFields().collect(toSet()), is(empty()));
    }

    @Test
    public void getOnChangeFields_WhenHasOnCreateOrUpdate() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID)
                .withInternalFields(ON_CREATE_OR_UPDATE, AuditedType.NAME, AuditedType.DESC)
                .build();

        final Set<EntityField<?, ?>> expectedOnChangeFields =
            ImmutableSet.of(AuditedType.NAME, AuditedType.DESC);

        assertThat(auditedFieldSet.getOnChangeFields().collect(toSet()), is(expectedOnChangeFields));
    }

    @Test
    public void getOnChangeFields_WhenHasOnUpdate() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID)
                .withInternalFields(ON_UPDATE, AuditedType.NAME, AuditedType.DESC)
                .build();

        final Set<EntityField<?, ?>> expectedOnChangeFields =
            ImmutableSet.of(AuditedType.NAME, AuditedType.DESC);

        assertThat(auditedFieldSet.getOnChangeFields().collect(toSet()), is(expectedOnChangeFields));
    }

    @Test
    public void getOnChangeFields_WhenHasExternalMandatory() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID)
                .withExternalFields(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC)
                .build();

        assertThat(auditedFieldSet.getOnChangeFields().collect(toSet()), is(empty()));
    }

    @Test
    public void getOnChangeFields_WhenHasInternalMandatory() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID)
                .withInternalFields(ALWAYS, AuditedType.NAME, AuditedType.DESC)
                .build();

        assertThat(auditedFieldSet.getOnChangeFields().collect(toSet()), is(empty()));
    }

    @Test
    public void getOnChangeFields_WhenHasOnCreateOrUpdateAndOnUpdate() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID)
                .withInternalFields(ON_CREATE_OR_UPDATE, AuditedType.NAME, AuditedType.DESC)
                .withInternalFields(ON_UPDATE, AuditedType.DESC2)
                .build();

        final Set<EntityField<?, ?>> expectedOnChangeFields =
            ImmutableSet.of(AuditedType.NAME,
                            AuditedType.DESC,
                            AuditedType.DESC2);

        assertThat(auditedFieldSet.getOnChangeFields().collect(toSet()), is(expectedOnChangeFields));
    }

    @Test
    public void getOnChangeFields_WhenHasOnCreateOrUpdateAndInternalMandatory() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID)
                .withInternalFields(ALWAYS, AuditedType.NAME)
                .withInternalFields(ON_CREATE_OR_UPDATE, AuditedType.DESC, AuditedType.DESC2)
                .build();

        final Set<EntityField<?, ?>> expectedOnChangeFields =
            ImmutableSet.of(AuditedType.DESC, AuditedType.DESC2);

        assertThat(auditedFieldSet.getOnChangeFields().collect(toSet()), is(expectedOnChangeFields));
    }

    @Test
    public void getOnChangeFields_WhenHasNone() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID).build();

        assertThat(auditedFieldSet.getOnChangeFields().collect(toSet()), is(empty()));
    }

    @Test
    public void getAllMandatoryFields_WhenHasExternalMandatory() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID)
                .withExternalFields(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC)
                .build();

        final Set<EntityField<?, ?>> expectedAllFields =
            ImmutableSet.of(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC);

        assertThat(auditedFieldSet.getMandatoryFields().collect(toSet()), is(expectedAllFields));
    }

    @Test
    public void getAllMandatoryFields_WhenHasInternalMandatory() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID)
                .withExternalFields(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC)
                .build();

        final Set<EntityField<?, ?>> expectedAllFields =
            ImmutableSet.of(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC);

        assertThat(auditedFieldSet.getMandatoryFields().collect(toSet()), is(expectedAllFields));
    }

    @Test
    public void getAllMandatoryFields_WhenHasExternalAndInternalMandatory() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID)
                .withExternalFields(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC)
                .withInternalFields(ALWAYS, AuditedType.NAME, AuditedType.DESC)
                .build();

        final Set<EntityField<?, ?>> expectedAllFields =
            ImmutableSet.of(NotAuditedAncestorType.NAME,
                            NotAuditedAncestorType.DESC,
                            AuditedType.NAME,
                            AuditedType.DESC);

        assertThat(auditedFieldSet.getMandatoryFields().collect(toSet()), is(expectedAllFields));
    }

    @Test
    public void getAllMandatoryFields_WhenHasNoMandatory() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID)
                .withInternalFields(ON_CREATE_OR_UPDATE, AuditedType.NAME, AuditedType.DESC)
                .build();

        assertThat(auditedFieldSet.getMandatoryFields().collect(toSet()), is(empty()));
    }

    @Test
    public void hasInternalFields_WhenHasOnCreateOrUpdateFields_ShouldReturnTrue() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID)
                .withInternalFields(ON_CREATE_OR_UPDATE, AuditedType.NAME, AuditedType.DESC)
                .build();

        assertThat(auditedFieldSet.hasInternalFields(), is(true));
    }

    @Test
    public void hasInternalFields_WhenHasOnUpdateFields_ShouldReturnTrue() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID)
                .withInternalFields(ON_UPDATE, AuditedType.NAME, AuditedType.DESC)
                .build();

        assertThat(auditedFieldSet.hasInternalFields(), is(true));
    }

    @Test
    public void hasInternalFields_WhenHasInternalMandatoryFields_ShouldReturnTrue() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID)
                .withInternalFields(ALWAYS, AuditedType.NAME, AuditedType.DESC)
                .build();

        assertThat(auditedFieldSet.hasInternalFields(), is(true));
    }

    @Test
    public void hasInternalFields_WhenHasIdOnly_ShouldReturnFalse() {
        final AuditedFieldSet<AuditedType> auditedFieldSet = builder(AuditedType.ID).build();

        assertThat(auditedFieldSet.hasInternalFields(), is(false));
    }

    @Test
    public void hasInternalFields_WhenHasIdAndExternalMandatoryOnly_ShouldReturnFalse() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID)
                .withExternalFields(NotAuditedAncestorType.NAME)
                .build();

        assertThat(auditedFieldSet.hasInternalFields(), is(false));
    }
}