package com.kenshoo.pl.entity.internal.audit;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.kenshoo.pl.entity.CurrentEntityState;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.FinalEntityState;
import com.kenshoo.pl.entity.audit.FieldAuditRecord;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedAutoIncIdType;
import org.jooq.lambda.Seq;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuditFieldChangesGeneratorTest {

    @Mock
    private AuditFieldChangeGenerator singleGenerator;

    @Mock
    private CurrentEntityState currentState;

    @Mock
    private FinalEntityState finalState;

    @Test
    public void generate_twoFields_BothGenerated_ShouldReturnBothFieldChanges() {
        final List<FieldAuditRecord> expectedFieldChanges = ImmutableList.of(mockFieldChange(), mockFieldChange());

        Seq.of(AuditedAutoIncIdType.NAME, AuditedAutoIncIdType.DESC)
           .zipWithIndex()
           .forEach(fieldWithIdx ->
                        when(singleGenerator.generate(currentState, finalState, AuditedField.builder(fieldWithIdx.v1).build()))
                            .thenReturn(Optional.of(expectedFieldChanges.get(fieldWithIdx.v2.intValue())))
                   );

        final Collection<FieldAuditRecord> actualFieldChanges =
            newGenerator(Stream.of(AuditedAutoIncIdType.NAME, AuditedAutoIncIdType.DESC)).generate(currentState, finalState);

        final List<FieldAuditRecord> sortedActualFieldChanges =
            actualFieldChanges.stream()
                              .sorted(Comparator.comparing(expectedFieldChanges::indexOf))
                              .collect(toList());

        assertThat(sortedActualFieldChanges, is(expectedFieldChanges));
    }

    @Test
    public void generate_twoFields_OnlyFirstGenerated_ShouldReturnFirstFieldChange() {
        final FieldAuditRecord expectedFieldChange = mockFieldChange();

        when(singleGenerator.generate(currentState, finalState, AuditedField.builder(AuditedAutoIncIdType.NAME).build()))
            .thenReturn(Optional.of(expectedFieldChange));
        when(singleGenerator.generate(currentState, finalState, AuditedField.builder(AuditedAutoIncIdType.DESC).build()))
            .thenReturn(Optional.empty());

        final Collection<FieldAuditRecord> actualFieldChanges =
            newGenerator(Stream.of(AuditedAutoIncIdType.NAME, AuditedAutoIncIdType.DESC)).generate(currentState, finalState);

        assertThat(ImmutableSet.copyOf(actualFieldChanges), is(singleton(expectedFieldChange)));
    }

    @Test
    public void generate_twoFields_OnlySecondGenerated_ShouldReturnSecondFieldChange() {
        final FieldAuditRecord expectedFieldChange = mockFieldChange();

        when(singleGenerator.generate(currentState, finalState, AuditedField.builder(AuditedAutoIncIdType.NAME).build()))
            .thenReturn(Optional.empty());
        when(singleGenerator.generate(currentState, finalState, AuditedField.builder(AuditedAutoIncIdType.DESC).build()))
            .thenReturn(Optional.of(expectedFieldChange));

        final Collection<FieldAuditRecord> actualFieldChanges =
            newGenerator(Stream.of(AuditedAutoIncIdType.NAME, AuditedAutoIncIdType.DESC)).generate(currentState, finalState);

        assertThat(ImmutableSet.copyOf(actualFieldChanges), is(singleton(expectedFieldChange)));
    }

    @Test
    public void generate_twoFields_NoneGenerated_ShouldReturnEmpty() {
        when(singleGenerator.generate(currentState, finalState, AuditedField.builder(AuditedAutoIncIdType.NAME).build()))
            .thenReturn(Optional.empty());
        when(singleGenerator.generate(currentState, finalState, AuditedField.builder(AuditedAutoIncIdType.DESC).build()))
            .thenReturn(Optional.empty());

        final Collection<FieldAuditRecord> actualFieldChanges =
            newGenerator(Stream.of(AuditedAutoIncIdType.NAME, AuditedAutoIncIdType.DESC)).generate(currentState, finalState);

        assertThat(actualFieldChanges, empty());
    }

    @Test
    public void generate_noFields_ShouldReturnEmpty() {
        final Collection<FieldAuditRecord> actualFieldChanges =
            newGenerator(Stream.empty()).generate(currentState, finalState);

        assertThat(actualFieldChanges, empty());
    }

    private AuditFieldChangesGenerator<AuditedAutoIncIdType> newGenerator(final Stream<? extends EntityField<AuditedAutoIncIdType, ?>> onChangeFields) {
        return new AuditFieldChangesGenerator<>(onChangeFields.map(f -> AuditedField.builder(f).build()), singleGenerator);
    }

    private FieldAuditRecord mockFieldChange() {
        return mock(FieldAuditRecord.class);
    }
}