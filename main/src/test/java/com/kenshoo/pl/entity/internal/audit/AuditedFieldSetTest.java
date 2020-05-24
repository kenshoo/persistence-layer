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
    public void getAllFields_IdAndAlwaysFields() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID)
                .withAlwaysFields(ImmutableSet.of(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC))
                .build();

        final Set<EntityField<?, ?>> expectedAllFields =
            ImmutableSet.of(AuditedType.ID,
                            NotAuditedAncestorType.NAME,
                            NotAuditedAncestorType.DESC);

        assertThat(auditedFieldSet.getAllFields().collect(toSet()), is(expectedAllFields));
    }

    @Test
    public void getAllFields_IdAndOnChangeFields() {
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
    public void getAllFields_IdAndAlwaysAndOnChangeFields() {
        final AuditedFieldSet<AuditedType> auditedFieldSet =
            builder(AuditedType.ID)
                .withAlwaysFields(ImmutableSet.of(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC))
                .withOnChangeFields(ImmutableSet.of(AuditedType.NAME, AuditedType.DESC))
                .build();

        final Set<EntityField<?, ?>> expectedAllFields =
            ImmutableSet.of(AuditedType.ID,
                            NotAuditedAncestorType.NAME,
                            NotAuditedAncestorType.DESC,
                            AuditedType.NAME,
                            AuditedType.DESC);

        assertThat(auditedFieldSet.getAllFields().collect(toSet()), is(expectedAllFields));
    }

    @Test
    public void intersectWith_WhenOthersAreExactlyIdAndOnChangeFields_ShouldReturnIdAndOnChangeFields() {
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
    public void intersectWith_WhenOthersAreExactlyOnChangeFields_ShouldReturnIdAndOnChangeFields() {
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
    public void intersectWith_WhenOthersContainIdAndOnChangeFields_ShouldReturnIdAndOnChangeFields() {
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
    public void intersectWith_WhenAuditedSetContainsOthersAndAlwaysFields_AndIdNotInOthers_ShouldReturnIdAndAlwaysFieldsAndOthers() {
        final Collection<? extends EntityField<AuditedType, ?>> otherFields = ImmutableSet.of(AuditedType.NAME);

        final AuditedFieldSet<AuditedType> initialAuditedFieldSet =
            builder(AuditedType.ID)
                .withAlwaysFields(ImmutableSet.of(NotAuditedAncestorType.NAME,
                                                  NotAuditedAncestorType.DESC))
                .withOnChangeFields(ImmutableSet.of(AuditedType.NAME,
                                                    AuditedType.DESC,
                                                    AuditedType.DESC2))
                .build();

        final AuditedFieldSet<AuditedType> expectedIntersectedAuditedFieldSet =
            builder(AuditedType.ID)
                .withAlwaysFields(ImmutableSet.of(NotAuditedAncestorType.NAME,
                                                  NotAuditedAncestorType.DESC))
                .withOnChangeFields(ImmutableSet.of(AuditedType.NAME))
                .build();

        final AuditedFieldSet<AuditedType> actualIntersectedAuditedFieldSet =
            initialAuditedFieldSet.intersectWith(otherFields.stream());

        assertThat(actualIntersectedAuditedFieldSet, is(expectedIntersectedAuditedFieldSet));
    }

    @Test
    public void intersectWith_WhenAuditedSetPartiallyIntersectsOthers_ShouldReturnIdAndIntersection() {
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
}