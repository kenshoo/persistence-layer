package com.kenshoo.pl.entity.internal.audit;

import com.google.common.collect.ImmutableSet;
import com.kenshoo.pl.entity.EntityField;
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

    private static final String ANCESTOR_NAME = "ancestorName";
    private static final String ANCESTOR_DESC = "ancestorDesc";

    @Mock
    private FinalEntityState finalState;

    @Test
    public void generate_twoFields_BothNotNull_ShouldReturnBothFieldValues() {
        when(finalState.safeGet(NotAuditedAncestorType.NAME)).thenReturn(Triptional.of(ANCESTOR_NAME));
        when(finalState.safeGet(NotAuditedAncestorType.DESC)).thenReturn(Triptional.of(ANCESTOR_DESC));

        final AuditMandatoryFieldValuesGenerator generator = newGenerator(Stream.of(NotAuditedAncestorType.NAME,
                                                                                    NotAuditedAncestorType.DESC));

        final Collection<? extends Entry<String, ?>> actualFieldValues = generator.generate(finalState);

        final Set<Entry<String, ?>> expectedFieldValues = Set.of(entry(NotAuditedAncestorType.NAME.toString(), ANCESTOR_NAME),
                                                                 entry(NotAuditedAncestorType.DESC.toString(), ANCESTOR_DESC));

        assertThat(ImmutableSet.copyOf(actualFieldValues), is(expectedFieldValues));
    }

    @Test
    public void generate_twoFields_BothNull_ShouldReturnEmpty() {
        Stream.of(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC)
              .forEach(field -> when(finalState.safeGet(field)).thenReturn(Triptional.nullInstance()));

        final AuditMandatoryFieldValuesGenerator generator = newGenerator(Stream.of(NotAuditedAncestorType.NAME,
                                                                                    NotAuditedAncestorType.DESC));

        final Collection<? extends Entry<String, ?>> actualFieldValues = generator.generate(finalState);

        assertThat(ImmutableSet.copyOf(actualFieldValues), is(empty()));
    }

    @Test
    public void generate_twoFields_BothAbsentShouldReturnEmpty() {
        Stream.of(NotAuditedAncestorType.NAME, NotAuditedAncestorType.DESC)
              .forEach(field -> when(finalState.safeGet(field)).thenReturn(Triptional.absent()));

        final AuditMandatoryFieldValuesGenerator generator = newGenerator(Stream.of(NotAuditedAncestorType.NAME,
                                                                                    NotAuditedAncestorType.DESC));

        final Collection<? extends Entry<String, ?>> actualFieldValues = generator.generate(finalState);

        assertThat(ImmutableSet.copyOf(actualFieldValues), is(empty()));
    }

    @Test
    public void generate_twoFields_FirstNotNull_SecondNull_ShouldReturnFirstFieldValue() {
        when(finalState.safeGet(NotAuditedAncestorType.NAME)).thenReturn(Triptional.of(ANCESTOR_NAME));
        when(finalState.safeGet(NotAuditedAncestorType.DESC)).thenReturn(Triptional.nullInstance());

        final AuditMandatoryFieldValuesGenerator generator = newGenerator(Stream.of(NotAuditedAncestorType.NAME,
                                                                                    NotAuditedAncestorType.DESC));


        final Set<Entry<String, ?>> expectedFieldValues = singleton(entry(NotAuditedAncestorType.NAME.toString(), ANCESTOR_NAME));

        final Collection<? extends Entry<String, ?>> actualFieldValues = generator.generate(finalState);

        assertThat(ImmutableSet.copyOf(actualFieldValues), is(expectedFieldValues));
    }

    @Test
    public void generate_twoFields_FirstAbsent_SecondNull_ShouldReturnSecondFieldValue() {
        when(finalState.safeGet(NotAuditedAncestorType.NAME)).thenReturn(Triptional.absent());
        when(finalState.safeGet(NotAuditedAncestorType.DESC)).thenReturn(Triptional.of(ANCESTOR_DESC));

        final AuditMandatoryFieldValuesGenerator generator = newGenerator(Stream.of(NotAuditedAncestorType.NAME,
                                                                                    NotAuditedAncestorType.DESC));


        final Set<Entry<String, ?>> expectedFieldValues = singleton(entry(NotAuditedAncestorType.DESC.toString(), ANCESTOR_DESC));

        final Collection<? extends Entry<String, ?>> actualFieldValues = generator.generate(finalState);

        assertThat(ImmutableSet.copyOf(actualFieldValues), is(expectedFieldValues));
    }

    @Test
    public void generate_noFields_ShouldReturnEmpty() {
        final AuditMandatoryFieldValuesGenerator generator = newGenerator(Stream.empty());

        assertThat(generator.generate(finalState), is(empty()));
    }

    private AuditMandatoryFieldValuesGenerator newGenerator(final Stream<? extends EntityField<?, ?>> mandatoryFields) {
        return new AuditMandatoryFieldValuesGenerator(mandatoryFields.map(AuditedField::new));
    }
}