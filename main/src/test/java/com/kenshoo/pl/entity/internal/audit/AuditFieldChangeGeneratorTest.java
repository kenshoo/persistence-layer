package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.CurrentEntityState;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.FinalEntityState;
import com.kenshoo.pl.entity.Triptional;
import com.kenshoo.pl.entity.audit.FieldAuditRecord;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAnd;
import static com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType.AMOUNT;
import static com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType.NAME;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

/**
 * ---- NOTE ----
 * The final state  is mocked here to make testing easier.<br>
 * However in practice the final state logic is such that it's impossible for the final state
 * to be 'absent' when the current state is 'present', therefore we don't need to test that.
 * This gap in the coverage is closed by the integration tests.
 */

@RunWith(MockitoJUnitRunner.class)
public class AuditFieldChangeGeneratorTest {

    @Mock
    private CurrentEntityState currentState;

    @Mock
    private FinalEntityState finalState;

    private final AuditFieldChangeGenerator generator = new AuditFieldChangeGenerator();

    @Test
    public void generate_CurrentNotNull_FinalNotNull_ChangedTrivially_ShouldReturnUpdatedFieldChange() {
        when(currentState.safeGet(NAME)).thenReturn(Triptional.of("old"));
        when(finalState.safeGet(NAME)).thenReturn(Triptional.of("new"));

        assertThat(generate(NAME),
                   isPresentAnd(is(FieldAuditRecord.builder(NAME)
                                                   .oldValue("old")
                                                   .newValue("new")
                                                   .build())));
    }

    @Test
    public void generate_CurrentNotNull_FinalNotNull_ChangedUsingEqFunction_ShouldReturnUpdatedFieldChange() {
        when(currentState.safeGet(AMOUNT)).thenReturn(Triptional.of(2.01));
        when(finalState.safeGet(AMOUNT)).thenReturn(Triptional.of(2.02));

        assertThat(generate(AMOUNT),
                   isPresentAnd(is(FieldAuditRecord.builder(AMOUNT)
                                                   .oldValue(2.01)
                                                   .newValue(2.02)
                                                   .build())));
    }

    @Test
    public void generate_CurrentNotNull_FinalNotNull_UnchangedTrivially_ShouldReturnEmpty() {
        when(currentState.safeGet(NAME)).thenReturn(Triptional.of("old"));
        when(finalState.safeGet(NAME)).thenReturn(Triptional.of("old"));

        assertThat(generate(NAME), isEmpty());
    }

    @Test
    public void generate_CurrentNotNull_FinalNotNull_UnchangedUsingEqFunction_ShouldReturnEmpty() {
        when(currentState.safeGet(AMOUNT)).thenReturn(Triptional.of(2.015));
        when(finalState.safeGet(AMOUNT)).thenReturn(Triptional.of(2.018));

        assertThat(generate(AMOUNT), isEmpty());
    }

    @Test
    public void generate_CurrentNotNull_FinalNull_ShouldReturnDeletedFieldChange() {
        when(currentState.safeGet(NAME)).thenReturn(Triptional.of("old"));
        when(finalState.safeGet(NAME)).thenReturn(Triptional.nullInstance());

        assertThat(generate(NAME),
                   isPresentAnd(is(FieldAuditRecord.builder(NAME).oldValue("old").build())));
    }

    @Test
    public void generate_CurrentNull_FinalNotNull_ShouldReturnCreatedFieldChange() {
        when(currentState.safeGet(NAME)).thenReturn(Triptional.nullInstance());
        when(finalState.safeGet(NAME)).thenReturn(Triptional.of("new"));

        assertThat(generate(NAME),
                   isPresentAnd(is(FieldAuditRecord.builder(NAME).newValue("new").build())));
    }

    @Test
    public void generate_CurrentNull_FinalNull_ShouldReturnEmpty() {
        when(currentState.safeGet(NAME)).thenReturn(Triptional.nullInstance());
        when(finalState.safeGet(NAME)).thenReturn(Triptional.nullInstance());

        assertThat(generate(NAME), isEmpty());
    }

    @Test
    public void generate_CurrentAbsent_FinalNotNull_ShouldReturnCreatedFieldChange() {
        when(currentState.safeGet(NAME)).thenReturn(Triptional.absent());
        when(finalState.safeGet(NAME)).thenReturn(Triptional.of("new"));

        assertThat(generate(NAME),
                   isPresentAnd(is(FieldAuditRecord.builder(NAME).newValue("new").build())));
    }

    @Test
    public void generate_CurrentAbsent_FinalNull_ShouldReturnFieldChangeWithEmptyContents() {
        when(currentState.safeGet(NAME)).thenReturn(Triptional.absent());
        when(finalState.safeGet(NAME)).thenReturn(Triptional.nullInstance());

        assertThat(generate(NAME), isPresentAnd(is(FieldAuditRecord.builder(NAME).build())));
    }

    @Test
    public void generate_CurrentAbsent_FinalAbsent_ShouldReturnEmpty() {
        when(currentState.safeGet(NAME)).thenReturn(Triptional.absent());
        when(finalState.safeGet(NAME)).thenReturn(Triptional.absent());

        assertThat(generate(NAME), isEmpty());
    }

    private Optional<? extends FieldAuditRecord> generate(final EntityField<AuditedType, ?> field) {
        return generator.generate(currentState, finalState, AuditedField.builder(field).build());
    }
}