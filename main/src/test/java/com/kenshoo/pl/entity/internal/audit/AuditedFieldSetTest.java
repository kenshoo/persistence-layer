package com.kenshoo.pl.entity.internal.audit;

import com.google.common.collect.ImmutableSet;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedAncestorType;
import org.junit.Test;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

import static com.kenshoo.pl.entity.internal.audit.AuditedFieldSet.builder;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.CoreMatchers.is;
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
                .withExternalMandatoryFields(ImmutableSet.of(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC))
                .build();

        final Set<EntityField<?, ?>> expectedAllFields =
            ImmutableSet.of(AuditedType.ID,
                            NotAuditedAncestorType.NAME,
                            NotAuditedAncestorType.DESC);

        assertThat(auditedFieldSet.getAllFields().collect(toSet()), is(expectedAllFields));
    }

    @Test
    public void getAllFields_IdAndSelfMandatory() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID)
                .withSelfMandatoryFields(AuditedType.NAME, AuditedType.DESC)
                .build();

        final Set<EntityField<?, ?>> expectedAllFields =
            ImmutableSet.of(AuditedType.ID,
                            AuditedType.NAME,
                            AuditedType.DESC);

        assertThat(auditedFieldSet.getAllFields().collect(toSet()), is(expectedAllFields));
    }

    @Test
    public void getAllFields_IdAndOnChange() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID)
                .withOnChangeFields(ImmutableSet.of(AuditedType.NAME, AuditedType.DESC))
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
                .withExternalMandatoryFields(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC)
                .withSelfMandatoryFields(AuditedType.NAME)
                .withOnChangeFields(AuditedType.DESC, AuditedType.DESC2)
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
    public void intersectWith_WhenOthersAreExactlyIdAndOnChange_ShouldReturnIdAndOnChange() {
        final Collection<? extends EntityField<AuditedType, ?>> otherFields = ImmutableSet.of(AuditedType.ID,
                                                                                              AuditedType.NAME,
                                                                                              AuditedType.DESC,
                                                                                              AuditedType.DESC2);

        final AuditedFieldSet<AuditedType> initialAuditedFieldSet =
            builder(AuditedType.ID)
                .withOnChangeFields(ImmutableSet.of(AuditedType.NAME,
                                                    AuditedType.DESC,
                                                    AuditedType.DESC2))
                .build();

        final AuditedFieldSet<AuditedType> actualIntersectedAuditedFieldSet =
            initialAuditedFieldSet.intersectWith(otherFields.stream());

        assertThat(actualIntersectedAuditedFieldSet, is(initialAuditedFieldSet));
    }

    @Test
    public void intersectWith_WhenOthersAreExactlyOnChange_ShouldReturnIdAndOnChange() {
        final Collection<? extends EntityField<AuditedType, ?>> otherFields = ImmutableSet.of(AuditedType.NAME,
                                                                                              AuditedType.DESC,
                                                                                              AuditedType.DESC2);
        final AuditedFieldSet<AuditedType> initialAuditedFieldSet =
            builder(AuditedType.ID)
                .withOnChangeFields(ImmutableSet.of(AuditedType.NAME,
                                                    AuditedType.DESC,
                                                    AuditedType.DESC2))
                .build();

        final AuditedFieldSet<AuditedType> actualIntersectedAuditedFieldSet =
            initialAuditedFieldSet.intersectWith(otherFields.stream());

        assertThat(actualIntersectedAuditedFieldSet, is(initialAuditedFieldSet));
    }

    @Test
    public void intersectWith_WhenOthersContainIdAndOnChange_ShouldReturnIdAndOnChange() {
        final Collection<? extends EntityField<AuditedType, ?>> otherFields = ImmutableSet.of(AuditedType.ID,
                                                                                              AuditedType.NAME,
                                                                                              AuditedType.DESC,
                                                                                              AuditedType.DESC2);

        final AuditedFieldSet<AuditedType> initialAuditedFieldSet =
            builder(AuditedType.ID)
                .withOnChangeFields(ImmutableSet.of(AuditedType.NAME,
                                                    AuditedType.DESC))
                .build();

        final AuditedFieldSet<AuditedType> actualIntersectedAuditedFieldSet =
            initialAuditedFieldSet.intersectWith(otherFields.stream());

        assertThat(actualIntersectedAuditedFieldSet, is(initialAuditedFieldSet));
    }

    @Test
    public void intersectWith_WhenAuditedSetContainsOthers_AndIdInOthers_ShouldReturnOthers() {
        final Collection<? extends EntityField<AuditedType, ?>> otherFields = ImmutableSet.of(AuditedType.ID,
                                                                                              AuditedType.NAME);

        final AuditedFieldSet<AuditedType> initialAuditedFieldSet =
            builder(AuditedType.ID)
                .withOnChangeFields(ImmutableSet.of(AuditedType.NAME,
                                                    AuditedType.DESC,
                                                    AuditedType.DESC2))
                .build();

        final AuditedFieldSet<AuditedType> expectedIntersectedAuditedFieldSet =
            builder(AuditedType.ID)
                .withOnChangeFields(ImmutableSet.of(AuditedType.NAME))
                .build();

        final AuditedFieldSet<AuditedType> actualIntersectedAuditedFieldSet =
            initialAuditedFieldSet.intersectWith(otherFields.stream());

        assertThat(actualIntersectedAuditedFieldSet, is(expectedIntersectedAuditedFieldSet));
    }

    @Test
    public void intersectWith_WhenAuditedSetContainsOthers_AndIdNotInOthers_ShouldReturnIdAndOthers() {
        final Collection<? extends EntityField<AuditedType, ?>> otherFields = ImmutableSet.of(AuditedType.NAME);

        final AuditedFieldSet<AuditedType> initialAuditedFieldSet =
            builder(AuditedType.ID)
                .withOnChangeFields(ImmutableSet.of(AuditedType.NAME,
                                                    AuditedType.DESC,
                                                    AuditedType.DESC2))
                .build();

        final AuditedFieldSet<AuditedType> expectedIntersectedAuditedFieldSet =
            builder(AuditedType.ID)
                .withOnChangeFields(ImmutableSet.of(AuditedType.NAME))
                .build();

        final AuditedFieldSet<AuditedType> actualIntersectedAuditedFieldSet =
            initialAuditedFieldSet.intersectWith(otherFields.stream());

        assertThat(actualIntersectedAuditedFieldSet, is(expectedIntersectedAuditedFieldSet));
    }

    @Test
    public void intersectWith_WhenAuditedSetContainsOthersAndExternalMandatory_AndIdNotInOthers_ShouldReturnIdAndExternalMandatoryAndOthers() {
        final Collection<? extends EntityField<AuditedType, ?>> otherFields = ImmutableSet.of(AuditedType.NAME);

        final AuditedFieldSet<AuditedType> initialAuditedFieldSet =
            builder(AuditedType.ID)
                .withExternalMandatoryFields(NotAuditedAncestorType.NAME,
                                             NotAuditedAncestorType.DESC)
                .withOnChangeFields(ImmutableSet.of(AuditedType.NAME,
                                                    AuditedType.DESC,
                                                    AuditedType.DESC2))
                .build();

        final AuditedFieldSet<AuditedType> expectedIntersectedAuditedFieldSet =
            builder(AuditedType.ID)
                .withExternalMandatoryFields(ImmutableSet.of(NotAuditedAncestorType.NAME,
                                                             NotAuditedAncestorType.DESC))
                .withOnChangeFields(ImmutableSet.of(AuditedType.NAME))
                .build();

        final AuditedFieldSet<AuditedType> actualIntersectedAuditedFieldSet =
            initialAuditedFieldSet.intersectWith(otherFields.stream());

        assertThat(actualIntersectedAuditedFieldSet, is(expectedIntersectedAuditedFieldSet));
    }

    @Test
    public void intersectWith_WhenAuditedSetPartiallyIntersects_ShouldReturnIdAndIntersection() {
        final Collection<? extends EntityField<AuditedType, ?>> otherFields = ImmutableSet.of(AuditedType.NAME,
                                                                                              AuditedType.DESC);

        final AuditedFieldSet<AuditedType> initialAuditedFieldSet =
            builder(AuditedType.ID)
                .withOnChangeFields(ImmutableSet.of(AuditedType.NAME,
                                                    AuditedType.DESC2))
                .build();

        final AuditedFieldSet<AuditedType> expectedIntersectedAuditedFieldSet =
            builder(AuditedType.ID)
                .withOnChangeFields(ImmutableSet.of(AuditedType.NAME))
                .build();

        final AuditedFieldSet<AuditedType> actualIntersectedAuditedFieldSet =
            initialAuditedFieldSet.intersectWith(otherFields.stream());

        assertThat(actualIntersectedAuditedFieldSet, is(expectedIntersectedAuditedFieldSet));
    }

    @Test
    public void intersectWith_WhenOthersAreEmpty_ShouldReturnId() {
        final AuditedFieldSet<AuditedType> initialAuditedFieldSet =
            builder(AuditedType.ID)
                .withOnChangeFields(ImmutableSet.of(AuditedType.NAME,
                                                    AuditedType.DESC))
                .build();

        assertThat(initialAuditedFieldSet.intersectWith(Stream.empty()),
                   is(builder(AuditedType.ID).build()));
    }

    @Test
    public void setExternalMandatoryFields_WhenOtherFieldTypesExist_AndInputNotEmpty_ShouldReturnEverything() {
        final AuditedFieldSet<AuditedType> initialAuditedFieldSet =
            builder(AuditedType.ID)
                .withSelfMandatoryFields(AuditedType.NAME)
                .withOnChangeFields(AuditedType.DESC, AuditedType.DESC2)
                .build();

        final AuditedFieldSet<AuditedType> expectedAuditedFieldSet =
            builder(AuditedType.ID)
                .withExternalMandatoryFields(NotAuditedAncestorType.NAME,
                                             NotAuditedAncestorType.DESC)
                .withSelfMandatoryFields(AuditedType.NAME)
                .withOnChangeFields(AuditedType.DESC, AuditedType.DESC2)
                .build();

        assertThat(initialAuditedFieldSet.setExternalMandatoryFields(ImmutableSet.of(NotAuditedAncestorType.NAME,
                                                                                     NotAuditedAncestorType.DESC)),
                   is(expectedAuditedFieldSet));
    }

    @Test
    public void setExternalMandatoryFields_WhenHasOnlyId_AndInputNotEmpty_ShouldReturnIdAndMandatory() {
        final AuditedFieldSet<AuditedType> initialAuditedFieldSet = builder(AuditedType.ID).build();

        final AuditedFieldSet<AuditedType> expectedAuditedFieldSet =
            builder(AuditedType.ID)
                .withExternalMandatoryFields(NotAuditedAncestorType.NAME,
                                             NotAuditedAncestorType.DESC)
                .build();

        assertThat(initialAuditedFieldSet.setExternalMandatoryFields(ImmutableSet.of(NotAuditedAncestorType.NAME,
                                                                                     NotAuditedAncestorType.DESC)),
                   is(expectedAuditedFieldSet));
    }

    @Test
    public void setExternalMandatoryFields_WhenHasExternalMandatory_AndInputNotEmpty_ShouldReplaceMandatory() {
        final AuditedFieldSet<AuditedType> initialAuditedFieldSet =
            builder(AuditedType.ID)
                .withExternalMandatoryFields(NotAuditedAncestorType.NAME)
                .build();

        final AuditedFieldSet<AuditedType> expectedAuditedFieldSet =
            builder(AuditedType.ID)
                .withExternalMandatoryFields(NotAuditedAncestorType.DESC)
                .build();

        assertThat(initialAuditedFieldSet.setExternalMandatoryFields(ImmutableSet.of(NotAuditedAncestorType.DESC)),
                   is(expectedAuditedFieldSet));
    }

    @Test
    public void setExternalMandatoryFields_WhenHasExternalMandatory_AndInputEmpty_ShouldEmptyMandatory() {
        final AuditedFieldSet<AuditedType> initialAuditedFieldSet =
            builder(AuditedType.ID)
                .withExternalMandatoryFields(NotAuditedAncestorType.NAME)
                .build();

        final AuditedFieldSet<AuditedType> expectedAuditedFieldSet =
            builder(AuditedType.ID)
                .build();

        assertThat(initialAuditedFieldSet.setExternalMandatoryFields(emptySet()),
                   is(expectedAuditedFieldSet));
    }

    @Test
    public void hasOnChangeFields_WhenExist_ShouldReturnTrue() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID)
                .withOnChangeFields(ImmutableSet.of(AuditedType.NAME,
                                                    AuditedType.DESC))
                .build();

        assertThat(auditedFieldSet.hasOnChangeFields(), is(true));
    }

    @Test
    public void hasOnChangeFields_WhenDontExist_ShouldReturnFalse() {
        final AuditedFieldSet<AuditedType> auditedFieldSet = builder(AuditedType.ID).build();

        assertThat(auditedFieldSet.hasOnChangeFields(), is(false));
    }
}