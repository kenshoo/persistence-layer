package com.kenshoo.pl.entity.internal.audit;

import com.google.common.collect.ImmutableSet;
import com.kenshoo.pl.entity.EntityField;
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
        final AuditedFieldSet<TestAuditedEntityType> auditedFieldSet =
            new AuditedFieldSet<>(TestAuditedEntityType.ID);

        final Set<EntityField<TestAuditedEntityType, ?>> expectedAllFields = singleton(TestAuditedEntityType.ID);

        assertThat(auditedFieldSet.getAllFields().collect(toSet()), is(expectedAllFields));
    }

    @Test
    public void getAllFields_IdAndDataFields() {
        final AuditedFieldSet<TestAuditedEntityType> auditedFieldSet =
            new AuditedFieldSet<>(TestAuditedEntityType.ID,
                                  ImmutableSet.of(TestAuditedEntityType.NAME,
                                                  TestAuditedEntityType.DESC));

        final Set<EntityField<TestAuditedEntityType, ?>> expectedAllFields =
            ImmutableSet.of(TestAuditedEntityType.ID,
                            TestAuditedEntityType.NAME,
                            TestAuditedEntityType.DESC);

        assertThat(auditedFieldSet.getAllFields().collect(toSet()), is(expectedAllFields));
    }

    @Test
    public void intersectWith_WhenOthersAreExactlyIdAndAuditedFields_ShouldReturnIdAndAuditedFields() {
        final Collection<? extends EntityField<TestAuditedEntityType, ?>> otherFields = ImmutableSet.of(TestAuditedEntityType.ID,
                                                                                                        TestAuditedEntityType.NAME,
                                                                                                        TestAuditedEntityType.DESC,
                                                                                                        TestAuditedEntityType.DESC2);

        final AuditedFieldSet<TestAuditedEntityType> initialAuditedFieldSet =
            new AuditedFieldSet<>(TestAuditedEntityType.ID,
                                  ImmutableSet.of(TestAuditedEntityType.NAME,
                                                  TestAuditedEntityType.DESC,
                                                  TestAuditedEntityType.DESC2));

        final AuditedFieldSet<TestAuditedEntityType> actualIntersectedAuditedFieldSet =
            initialAuditedFieldSet.intersectWith(otherFields.stream());

        assertThat(actualIntersectedAuditedFieldSet, is(initialAuditedFieldSet));
    }

    @Test
    public void intersectWith_WhenOthersAreExactlyAuditedFields_ShouldReturnIdAndAuditedFields() {
        final Collection<? extends EntityField<TestAuditedEntityType, ?>> otherFields = ImmutableSet.of(TestAuditedEntityType.NAME,
                                                                                                        TestAuditedEntityType.DESC,
                                                                                                        TestAuditedEntityType.DESC2);
        final AuditedFieldSet<TestAuditedEntityType> initialAuditedFieldSet =
            new AuditedFieldSet<>(TestAuditedEntityType.ID,
                                  ImmutableSet.of(TestAuditedEntityType.NAME,
                                                  TestAuditedEntityType.DESC,
                                                  TestAuditedEntityType.DESC2));


        final AuditedFieldSet<TestAuditedEntityType> actualIntersectedAuditedFieldSet =
            initialAuditedFieldSet.intersectWith(otherFields.stream());

        assertThat(actualIntersectedAuditedFieldSet, is(initialAuditedFieldSet));
    }

    @Test
    public void intersectWith_WhenOthersContainIdAndAuditedFields_ShouldReturnIdAndAuditedFields() {
        final Collection<? extends EntityField<TestAuditedEntityType, ?>> otherFields = ImmutableSet.of(TestAuditedEntityType.ID,
                                                                                                        TestAuditedEntityType.NAME,
                                                                                                        TestAuditedEntityType.DESC,
                                                                                                        TestAuditedEntityType.DESC2);

        final AuditedFieldSet<TestAuditedEntityType> initialAuditedFieldSet =
            new AuditedFieldSet<>(TestAuditedEntityType.ID,
                                  ImmutableSet.of(TestAuditedEntityType.NAME,
                                                  TestAuditedEntityType.DESC));

        final AuditedFieldSet<TestAuditedEntityType> actualIntersectedAuditedFieldSet =
            initialAuditedFieldSet.intersectWith(otherFields.stream());

        assertThat(actualIntersectedAuditedFieldSet, is(initialAuditedFieldSet));
    }

    @Test
    public void intersectWith_WhenAuditedSetContainsOthers_AndIdInOthers_ShouldReturnOthers() {
        final Collection<? extends EntityField<TestAuditedEntityType, ?>> otherFields = ImmutableSet.of(TestAuditedEntityType.ID,
                                                                                                        TestAuditedEntityType.NAME);

        final AuditedFieldSet<TestAuditedEntityType> initialAuditedFieldSet =
            new AuditedFieldSet<>(TestAuditedEntityType.ID,
                                  ImmutableSet.of(TestAuditedEntityType.NAME,
                                                  TestAuditedEntityType.DESC,
                                                  TestAuditedEntityType.DESC2));

        final AuditedFieldSet<TestAuditedEntityType> expectedIntersectedAuditedFieldSet =
            new AuditedFieldSet<>(TestAuditedEntityType.ID,
                                  ImmutableSet.of(TestAuditedEntityType.NAME));

        final AuditedFieldSet<TestAuditedEntityType> actualIntersectedAuditedFieldSet =
            initialAuditedFieldSet.intersectWith(otherFields.stream());

        assertThat(actualIntersectedAuditedFieldSet, is(expectedIntersectedAuditedFieldSet));
    }

    @Test
    public void intersectWith_WhenAuditedSetContainsOthers_AndIdNotInOthers_ShouldReturnIdAndOthers() {
        final Collection<? extends EntityField<TestAuditedEntityType, ?>> otherFields = ImmutableSet.of(TestAuditedEntityType.NAME);

        final AuditedFieldSet<TestAuditedEntityType> initialAuditedFieldSet =
            new AuditedFieldSet<>(TestAuditedEntityType.ID,
                                  ImmutableSet.of(TestAuditedEntityType.NAME,
                                                  TestAuditedEntityType.DESC,
                                                  TestAuditedEntityType.DESC2));

        final AuditedFieldSet<TestAuditedEntityType> expectedIntersectedAuditedFieldSet =
            new AuditedFieldSet<>(TestAuditedEntityType.ID,
                                  ImmutableSet.of(TestAuditedEntityType.NAME));

        final AuditedFieldSet<TestAuditedEntityType> actualIntersectedAuditedFieldSet =
            initialAuditedFieldSet.intersectWith(otherFields.stream());

        assertThat(actualIntersectedAuditedFieldSet, is(expectedIntersectedAuditedFieldSet));
    }

    @Test
    public void intersectWith_WhenAuditedSetPartiallyIntersectsOthers_ShouldReturnIdAndIntersection() {
        final Collection<? extends EntityField<TestAuditedEntityType, ?>> otherFields = ImmutableSet.of(TestAuditedEntityType.NAME,
                                                                                                        TestAuditedEntityType.DESC);

        final AuditedFieldSet<TestAuditedEntityType> initialAuditedFieldSet =
            new AuditedFieldSet<>(TestAuditedEntityType.ID,
                                  ImmutableSet.of(TestAuditedEntityType.NAME,
                                                  TestAuditedEntityType.DESC2));

        final AuditedFieldSet<TestAuditedEntityType> expectedIntersectedAuditedFieldSet =
            new AuditedFieldSet<>(TestAuditedEntityType.ID,
                                  ImmutableSet.of(TestAuditedEntityType.NAME));

        final AuditedFieldSet<TestAuditedEntityType> actualIntersectedAuditedFieldSet =
            initialAuditedFieldSet.intersectWith(otherFields.stream());

        assertThat(actualIntersectedAuditedFieldSet, is(expectedIntersectedAuditedFieldSet));
    }

    @Test
    public void intersectWith_WhenOthersAreEmpty_ShouldReturnId() {
        final AuditedFieldSet<TestAuditedEntityType> initialAuditedFieldSet =
            new AuditedFieldSet<>(TestAuditedEntityType.ID,
                                  ImmutableSet.of(TestAuditedEntityType.NAME,
                                                  TestAuditedEntityType.DESC));


        assertThat(initialAuditedFieldSet.intersectWith(Stream.empty()),
                   is(new AuditedFieldSet<>(TestAuditedEntityType.ID, emptySet())));
    }
}