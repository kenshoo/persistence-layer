package com.kenshoo.pl.entity.internal.audit;

import com.google.common.collect.ImmutableSet;
import com.kenshoo.pl.entity.FieldValue;
import com.kenshoo.pl.entity.FinalEntityState;
import com.kenshoo.pl.entity.Triptional;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedAncestorType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.singleton;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuditMandatoryFieldValuesGeneratorTest {

    private static final String ANCESTOR_NAME = "SomeAncestorName";
    private static final String ANCESTOR_DESC = "SomeAncestorDesc";

    private static final String ANCESTOR_NAME_FIELD_NAME = "ancestor_name";
    private static final String ANCESTOR_DESC_FIELD_NAME = "ancestor_desc";

    private static final AuditedField<NotAuditedAncestorType, String> ANCESTOR_NAME_AUDITED_FIELD =
        AuditedField.builder(NotAuditedAncestorType.NAME)
                    .withName(ANCESTOR_NAME_FIELD_NAME)
                    .build();

    private static final AuditedField<NotAuditedAncestorType, String> ANCESTOR_DESC_AUDITED_FIELD =
        AuditedField.builder(NotAuditedAncestorType.DESC)
                    .withName(ANCESTOR_DESC_FIELD_NAME)
                    .build();

    @Mock
    private FinalEntityState finalState;

    @Mock
    private AuditFieldValueResolver auditFieldValueResolver;

    @Test
    public void generate_twoFields_BothNotNull_ShouldReturnBothFieldValues() {
        final var auditedEntityType = AuditedEntityType.builder(AuditedType.ID)
                .withExternalFields(Stream.of(ANCESTOR_NAME_AUDITED_FIELD, ANCESTOR_DESC_AUDITED_FIELD))
                .build();

        when(auditFieldValueResolver.resolveToString(ANCESTOR_NAME_AUDITED_FIELD, finalState)).thenReturn(Triptional.of(ANCESTOR_NAME));
        when(auditFieldValueResolver.resolveToString(ANCESTOR_DESC_AUDITED_FIELD, finalState)).thenReturn(Triptional.of(ANCESTOR_DESC));

        final AuditMandatoryFieldValuesGenerator generator = newGenerator(auditedEntityType);

        final Collection<? extends FieldValue> actualFieldValues = generator.generate(finalState);

        final Set<FieldValue> expectedFieldValues = Set.of(new FieldValue(ANCESTOR_NAME_FIELD_NAME, ANCESTOR_NAME),
                                                           new FieldValue(ANCESTOR_DESC_FIELD_NAME, ANCESTOR_DESC));

        assertThat(ImmutableSet.copyOf(actualFieldValues), is(expectedFieldValues));
    }

    @Test
    public void generate_twoFields_BothNull_ShouldReturnEmpty() {
        final var auditedEntityType = AuditedEntityType.builder(AuditedType.ID)
                .withExternalFields(Stream.of(ANCESTOR_NAME_AUDITED_FIELD, ANCESTOR_DESC_AUDITED_FIELD))
                .build();

        Stream.of(ANCESTOR_NAME_AUDITED_FIELD, ANCESTOR_DESC_AUDITED_FIELD)
              .forEach(field -> when(auditFieldValueResolver.resolveToString(field, finalState)).thenReturn(Triptional.nullInstance()));

        final AuditMandatoryFieldValuesGenerator generator = newGenerator(auditedEntityType);

        final Collection<? extends FieldValue> actualFieldValues = generator.generate(finalState);

        assertThat(ImmutableSet.copyOf(actualFieldValues), is(empty()));
    }

    @Test
    public void generate_twoFields_BothAbsentShouldReturnEmpty() {
        final var auditedEntityType = AuditedEntityType.builder(AuditedType.ID)
                .withExternalFields(Stream.of(ANCESTOR_NAME_AUDITED_FIELD, ANCESTOR_DESC_AUDITED_FIELD))
                .build();

        Stream.of(ANCESTOR_NAME_AUDITED_FIELD, ANCESTOR_DESC_AUDITED_FIELD)
              .forEach(field -> when(auditFieldValueResolver.resolveToString(field, finalState)).thenReturn(Triptional.absent()));

        final AuditMandatoryFieldValuesGenerator generator = newGenerator(auditedEntityType);

        final Collection<? extends FieldValue> actualFieldValues = generator.generate(finalState);

        assertThat(ImmutableSet.copyOf(actualFieldValues), is(empty()));
    }

    @Test
    public void generate_twoFields_FirstNotNull_SecondNull_ShouldReturnFirstFieldValue() {
        final var auditedEntityType = AuditedEntityType.builder(AuditedType.ID)
                .withExternalFields(Stream.of(ANCESTOR_NAME_AUDITED_FIELD, ANCESTOR_DESC_AUDITED_FIELD))
                .build();

        when(auditFieldValueResolver.resolveToString(ANCESTOR_NAME_AUDITED_FIELD, finalState)).thenReturn(Triptional.of(ANCESTOR_NAME));
        when(auditFieldValueResolver.resolveToString(ANCESTOR_DESC_AUDITED_FIELD, finalState)).thenReturn(Triptional.nullInstance());

        final AuditMandatoryFieldValuesGenerator generator = newGenerator(auditedEntityType);

        final Set<FieldValue> expectedFieldValues = singleton(new FieldValue(ANCESTOR_NAME_FIELD_NAME, ANCESTOR_NAME));

        final Collection<? extends FieldValue> actualFieldValues = generator.generate(finalState);

        assertThat(ImmutableSet.copyOf(actualFieldValues), is(expectedFieldValues));
    }

    @Test
    public void generate_twoFields_FirstAbsent_SecondNull_ShouldReturnSecondFieldValue() {
        final var auditedEntityType = AuditedEntityType.builder(AuditedType.ID)
                .withExternalFields(Stream.of(ANCESTOR_NAME_AUDITED_FIELD, ANCESTOR_DESC_AUDITED_FIELD))
                .build();

        when(auditFieldValueResolver.resolveToString(ANCESTOR_NAME_AUDITED_FIELD, finalState)).thenReturn(Triptional.absent());
        when(auditFieldValueResolver.resolveToString(ANCESTOR_DESC_AUDITED_FIELD, finalState)).thenReturn(Triptional.of(ANCESTOR_DESC));

        final AuditMandatoryFieldValuesGenerator generator = newGenerator(auditedEntityType);


        final Set<FieldValue> expectedFieldValues = singleton(new FieldValue(ANCESTOR_DESC_FIELD_NAME, ANCESTOR_DESC));

        final Collection<? extends FieldValue> actualFieldValues = generator.generate(finalState);

        assertThat(ImmutableSet.copyOf(actualFieldValues), is(expectedFieldValues));
    }

    @Test
    public void generate_noFields_ShouldReturnEmpty() {
        final var auditedEntityType = AuditedEntityType.builder(AuditedType.ID).build();

        final AuditMandatoryFieldValuesGenerator generator = newGenerator(auditedEntityType);

        assertThat(generator.generate(finalState), is(empty()));
    }

    private AuditMandatoryFieldValuesGenerator newGenerator(final AuditedEntityType<?> auditedEntityType) {
        return new AuditMandatoryFieldValuesGenerator(auditedEntityType, auditFieldValueResolver);
    }
}