package com.kenshoo.pl.entity.internal.audit;

import com.google.common.collect.ImmutableSet;
import com.kenshoo.pl.entity.FinalEntityState;
import com.kenshoo.pl.entity.Triptional;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedAncestorType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.singleton;
import static java.util.Map.entry;
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
        when(auditFieldValueResolver.resolveToString(ANCESTOR_NAME_AUDITED_FIELD, finalState)).thenReturn(Triptional.of(ANCESTOR_NAME));
        when(auditFieldValueResolver.resolveToString(ANCESTOR_DESC_AUDITED_FIELD, finalState)).thenReturn(Triptional.of(ANCESTOR_DESC));

        final AuditMandatoryFieldValuesGenerator generator = newGenerator(Stream.of(ANCESTOR_NAME_AUDITED_FIELD,
                                                                                    ANCESTOR_DESC_AUDITED_FIELD));

        final Collection<? extends Entry<String, ?>> actualFieldValues = generator.generate(finalState);

        final Set<Entry<String, ?>> expectedFieldValues = Set.of(entry(ANCESTOR_NAME_FIELD_NAME, ANCESTOR_NAME),
                                                                 entry(ANCESTOR_DESC_FIELD_NAME, ANCESTOR_DESC));

        assertThat(ImmutableSet.copyOf(actualFieldValues), is(expectedFieldValues));
    }

    @Test
    public void generate_twoFields_BothNull_ShouldReturnEmpty() {
        Stream.of(ANCESTOR_NAME_AUDITED_FIELD, ANCESTOR_DESC_AUDITED_FIELD)
              .forEach(field -> when(auditFieldValueResolver.resolveToString(field, finalState)).thenReturn(Triptional.nullInstance()));

        final AuditMandatoryFieldValuesGenerator generator = newGenerator(Stream.of(ANCESTOR_NAME_AUDITED_FIELD,
                                                                                    ANCESTOR_DESC_AUDITED_FIELD));

        final Collection<? extends Entry<String, ?>> actualFieldValues = generator.generate(finalState);

        assertThat(ImmutableSet.copyOf(actualFieldValues), is(empty()));
    }

    @Test
    public void generate_twoFields_BothAbsentShouldReturnEmpty() {
        Stream.of(ANCESTOR_NAME_AUDITED_FIELD, ANCESTOR_DESC_AUDITED_FIELD)
              .forEach(field -> when(auditFieldValueResolver.resolveToString(field, finalState)).thenReturn(Triptional.absent()));

        final AuditMandatoryFieldValuesGenerator generator = newGenerator(Stream.of(ANCESTOR_NAME_AUDITED_FIELD,
                                                                                    ANCESTOR_DESC_AUDITED_FIELD));

        final Collection<? extends Entry<String, ?>> actualFieldValues = generator.generate(finalState);

        assertThat(ImmutableSet.copyOf(actualFieldValues), is(empty()));
    }

    @Test
    public void generate_twoFields_FirstNotNull_SecondNull_ShouldReturnFirstFieldValue() {
        when(auditFieldValueResolver.resolveToString(ANCESTOR_NAME_AUDITED_FIELD, finalState)).thenReturn(Triptional.of(ANCESTOR_NAME));
        when(auditFieldValueResolver.resolveToString(ANCESTOR_DESC_AUDITED_FIELD, finalState)).thenReturn(Triptional.nullInstance());

        final AuditMandatoryFieldValuesGenerator generator = newGenerator(Stream.of(ANCESTOR_NAME_AUDITED_FIELD,
                                                                                    ANCESTOR_DESC_AUDITED_FIELD));


        final Set<Entry<String, ?>> expectedFieldValues = singleton(entry(ANCESTOR_NAME_FIELD_NAME, ANCESTOR_NAME));

        final Collection<? extends Entry<String, ?>> actualFieldValues = generator.generate(finalState);

        assertThat(ImmutableSet.copyOf(actualFieldValues), is(expectedFieldValues));
    }

    @Test
    public void generate_twoFields_FirstAbsent_SecondNull_ShouldReturnSecondFieldValue() {
        when(auditFieldValueResolver.resolveToString(ANCESTOR_NAME_AUDITED_FIELD, finalState)).thenReturn(Triptional.absent());
        when(auditFieldValueResolver.resolveToString(ANCESTOR_DESC_AUDITED_FIELD, finalState)).thenReturn(Triptional.of(ANCESTOR_DESC));

        final AuditMandatoryFieldValuesGenerator generator = newGenerator(Stream.of(ANCESTOR_NAME_AUDITED_FIELD,
                                                                                    ANCESTOR_DESC_AUDITED_FIELD));


        final Set<Entry<String, ?>> expectedFieldValues = singleton(entry(ANCESTOR_DESC_FIELD_NAME, ANCESTOR_DESC));

        final Collection<? extends Entry<String, ?>> actualFieldValues = generator.generate(finalState);

        assertThat(ImmutableSet.copyOf(actualFieldValues), is(expectedFieldValues));
    }

    @Test
    public void generate_noFields_ShouldReturnEmpty() {
        final AuditMandatoryFieldValuesGenerator generator = newGenerator(Stream.empty());

        assertThat(generator.generate(finalState), is(empty()));
    }

    private AuditMandatoryFieldValuesGenerator newGenerator(final Stream<? extends AuditedField<?, ?>> mandatoryFields) {
        return new AuditMandatoryFieldValuesGenerator(mandatoryFields, auditFieldValueResolver);
    }
}