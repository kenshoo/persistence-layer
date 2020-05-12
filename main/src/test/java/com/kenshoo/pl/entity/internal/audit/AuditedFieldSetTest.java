package com.kenshoo.pl.entity.internal.audit;

import com.google.common.collect.ImmutableSet;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType;
import org.junit.Test;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AuditedFieldSetTest {

    @Test
    public void getAllFields_IdOnly() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            new AuditedFieldSet<>(AuditedType.ID);

        final Set<EntityField<AuditedType, ?>> expectedAllFields = singleton(AuditedType.ID);

        assertThat(auditedFieldSet.getAllFields().collect(toSet()), is(expectedAllFields));
    }

    @Test
    public void getAllFields_IdAndDataFields() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            new AuditedFieldSet<>(AuditedType.ID,
                                  ImmutableSet.of(AuditedType.NAME,
                                                  AuditedType.DESC));

        final Set<EntityField<AuditedType, ?>> expectedAllFields =
            ImmutableSet.of(AuditedType.ID,
                            AuditedType.NAME,
                            AuditedType.DESC);

        assertThat(auditedFieldSet.getAllFields().collect(toSet()), is(expectedAllFields));
    }

    @Test
    public void intersectWith_WhenOthersAreExactlyIdAndAuditedFields_ShouldReturnIdAndAuditedFields() {
        final Collection<? extends EntityField<AuditedType, ?>> otherFields = ImmutableSet.of(AuditedType.ID,
                                                                                              AuditedType.NAME,
                                                                                              AuditedType.DESC,
                                                                                              AuditedType.DESC2);

        final AuditedFieldSet<AuditedType> initialAuditedFieldSet =
            new AuditedFieldSet<>(AuditedType.ID,
                                  ImmutableSet.of(AuditedType.NAME,
                                                  AuditedType.DESC,
                                                  AuditedType.DESC2));

        final AuditedFieldSet<AuditedType> actualIntersectedAuditedFieldSet =
            initialAuditedFieldSet.intersectWith(otherFields.stream());

        assertThat(actualIntersectedAuditedFieldSet, is(initialAuditedFieldSet));
    }

    @Test
    public void intersectWith_WhenOthersAreExactlyAuditedFields_ShouldReturnIdAndAuditedFields() {
        final Collection<? extends EntityField<AuditedType, ?>> otherFields = ImmutableSet.of(AuditedType.NAME,
                                                                                              AuditedType.DESC,
                                                                                              AuditedType.DESC2);
        final AuditedFieldSet<AuditedType> initialAuditedFieldSet =
            new AuditedFieldSet<>(AuditedType.ID,
                                  ImmutableSet.of(AuditedType.NAME,
                                                  AuditedType.DESC,
                                                  AuditedType.DESC2));


        final AuditedFieldSet<AuditedType> actualIntersectedAuditedFieldSet =
            initialAuditedFieldSet.intersectWith(otherFields.stream());

        assertThat(actualIntersectedAuditedFieldSet, is(initialAuditedFieldSet));
    }

    @Test
    public void intersectWith_WhenOthersContainIdAndAuditedFields_ShouldReturnIdAndAuditedFields() {
        final Collection<? extends EntityField<AuditedType, ?>> otherFields = ImmutableSet.of(AuditedType.ID,
                                                                                              AuditedType.NAME,
                                                                                              AuditedType.DESC,
                                                                                              AuditedType.DESC2);

        final AuditedFieldSet<AuditedType> initialAuditedFieldSet =
            new AuditedFieldSet<>(AuditedType.ID,
                                  ImmutableSet.of(AuditedType.NAME,
                                                  AuditedType.DESC));

        final AuditedFieldSet<AuditedType> actualIntersectedAuditedFieldSet =
            initialAuditedFieldSet.intersectWith(otherFields.stream());

        assertThat(actualIntersectedAuditedFieldSet, is(initialAuditedFieldSet));
    }

    @Test
    public void intersectWith_WhenAuditedSetContainsOthers_AndIdInOthers_ShouldReturnOthers() {
        final Collection<? extends EntityField<AuditedType, ?>> otherFields = ImmutableSet.of(AuditedType.ID,
                                                                                              AuditedType.NAME);

        final AuditedFieldSet<AuditedType> initialAuditedFieldSet =
            new AuditedFieldSet<>(AuditedType.ID,
                                  ImmutableSet.of(AuditedType.NAME,
                                                  AuditedType.DESC,
                                                  AuditedType.DESC2));

        final AuditedFieldSet<AuditedType> expectedIntersectedAuditedFieldSet =
            new AuditedFieldSet<>(AuditedType.ID,
                                  ImmutableSet.of(AuditedType.NAME));

        final AuditedFieldSet<AuditedType> actualIntersectedAuditedFieldSet =
            initialAuditedFieldSet.intersectWith(otherFields.stream());

        assertThat(actualIntersectedAuditedFieldSet, is(expectedIntersectedAuditedFieldSet));
    }

    @Test
    public void intersectWith_WhenAuditedSetContainsOthers_AndIdNotInOthers_ShouldReturnIdAndOthers() {
        final Collection<? extends EntityField<AuditedType, ?>> otherFields = ImmutableSet.of(AuditedType.NAME);

        final AuditedFieldSet<AuditedType> initialAuditedFieldSet =
            new AuditedFieldSet<>(AuditedType.ID,
                                  ImmutableSet.of(AuditedType.NAME,
                                                  AuditedType.DESC,
                                                  AuditedType.DESC2));

        final AuditedFieldSet<AuditedType> expectedIntersectedAuditedFieldSet =
            new AuditedFieldSet<>(AuditedType.ID,
                                  ImmutableSet.of(AuditedType.NAME));

        final AuditedFieldSet<AuditedType> actualIntersectedAuditedFieldSet =
            initialAuditedFieldSet.intersectWith(otherFields.stream());

        assertThat(actualIntersectedAuditedFieldSet, is(expectedIntersectedAuditedFieldSet));
    }

    @Test
    public void intersectWith_WhenAuditedSetPartiallyIntersectsOthers_ShouldReturnIdAndIntersection() {
        final Collection<? extends EntityField<AuditedType, ?>> otherFields = ImmutableSet.of(AuditedType.NAME,
                                                                                              AuditedType.DESC);

        final AuditedFieldSet<AuditedType> initialAuditedFieldSet =
            new AuditedFieldSet<>(AuditedType.ID,
                                  ImmutableSet.of(AuditedType.NAME,
                                                  AuditedType.DESC2));

        final AuditedFieldSet<AuditedType> expectedIntersectedAuditedFieldSet =
            new AuditedFieldSet<>(AuditedType.ID,
                                  ImmutableSet.of(AuditedType.NAME));

        final AuditedFieldSet<AuditedType> actualIntersectedAuditedFieldSet =
            initialAuditedFieldSet.intersectWith(otherFields.stream());

        assertThat(actualIntersectedAuditedFieldSet, is(expectedIntersectedAuditedFieldSet));
    }

    @Test
    public void intersectWith_WhenOthersAreEmpty_ShouldReturnId() {
        final AuditedFieldSet<AuditedType> initialAuditedFieldSet =
            new AuditedFieldSet<>(AuditedType.ID,
                                  ImmutableSet.of(AuditedType.NAME,
                                                  AuditedType.DESC));


        assertThat(initialAuditedFieldSet.intersectWith(Stream.empty()),
                   is(new AuditedFieldSet<>(AuditedType.ID, emptySet())));
    }
}