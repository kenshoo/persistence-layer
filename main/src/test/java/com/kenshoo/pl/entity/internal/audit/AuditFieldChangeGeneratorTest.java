package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.CurrentEntityState;
import com.kenshoo.pl.entity.FinalEntityState;
import com.kenshoo.pl.entity.Triptional;
import com.kenshoo.pl.entity.audit.FieldAuditRecord;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
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

    private static final String NAME_FIELD_NAME = "name";
    private static final String AMOUNT_FIELD_NAME = "amount";

    private static final AuditedField<AuditedType, String> NAME_AUDITED_FIELD = AuditedField.builder(NAME)
                                                                                            .withName(NAME_FIELD_NAME)
                                                                                            .build();
    private static final AuditedField<AuditedType, Double> AMOUNT_AUDITED_FIELD = AuditedField.builder(AMOUNT)
                                                                                              .withName(AMOUNT_FIELD_NAME)
                                                                                              .build();

    private static final String OLD_NAME = "old";
    private static final String NEW_NAME = "new";

    @Mock
    private CurrentEntityState currentState;

    @Mock
    private FinalEntityState finalState;

    @Mock
    private AuditFieldValueResolver auditFieldValueResolver;

    @InjectMocks
    private AuditFieldChangeGenerator generator;

    @Test
    public void generate_CurrentNotNull_FinalNotNull_ChangedTrivially_ShouldReturnUpdatedFieldChange() {
        when(auditFieldValueResolver.resolve(NAME_AUDITED_FIELD, currentState)).thenReturn(Triptional.of(OLD_NAME));
        when(auditFieldValueResolver.resolve(NAME_AUDITED_FIELD, finalState)).thenReturn(Triptional.of(NEW_NAME));

        when(auditFieldValueResolver.resolveToString(NAME_AUDITED_FIELD, currentState)).thenReturn(Triptional.of(OLD_NAME));
        when(auditFieldValueResolver.resolveToString(NAME_AUDITED_FIELD, finalState)).thenReturn(Triptional.of(NEW_NAME));

        assertThat(generate(NAME_AUDITED_FIELD),
                   isPresentAnd(is(FieldAuditRecord.builder(NAME_FIELD_NAME)
                                                   .oldValue(OLD_NAME)
                                                   .newValue(NEW_NAME)
                                                   .build())));
    }

    @Test
    public void generate_CurrentNotNull_FinalNotNull_ChangedUsingEqFunction_ShouldReturnUpdatedFieldChange() {
        when(auditFieldValueResolver.resolve(AMOUNT_AUDITED_FIELD, currentState)).thenReturn(Triptional.of(2.01));
        when(auditFieldValueResolver.resolve(AMOUNT_AUDITED_FIELD, finalState)).thenReturn(Triptional.of(2.02));

        when(auditFieldValueResolver.resolveToString(AMOUNT_AUDITED_FIELD, currentState)).thenReturn(Triptional.of("2.01"));
        when(auditFieldValueResolver.resolveToString(AMOUNT_AUDITED_FIELD, finalState)).thenReturn(Triptional.of("2.02"));

        assertThat(generate(AMOUNT_AUDITED_FIELD),
                   isPresentAnd(is(FieldAuditRecord.builder(AMOUNT_FIELD_NAME)
                                                   .oldValue("2.01")
                                                   .newValue("2.02")
                                                   .build())));
    }

    @Test
    public void generate_CurrentNotNull_FinalNotNull_UnchangedTrivially_ShouldReturnEmpty() {
        when(auditFieldValueResolver.resolve(NAME_AUDITED_FIELD, currentState)).thenReturn(Triptional.of(OLD_NAME));
        when(auditFieldValueResolver.resolve(NAME_AUDITED_FIELD, finalState)).thenReturn(Triptional.of(OLD_NAME));

        assertThat(generate(NAME_AUDITED_FIELD), isEmpty());
    }

    @Test
    public void generate_CurrentNotNull_FinalNotNull_UnchangedUsingEqFunction_ShouldReturnEmpty() {
        when(auditFieldValueResolver.resolve(AMOUNT_AUDITED_FIELD, currentState)).thenReturn(Triptional.of(2.015));
        when(auditFieldValueResolver.resolve(AMOUNT_AUDITED_FIELD, finalState)).thenReturn(Triptional.of(2.018));

        assertThat(generate(AMOUNT_AUDITED_FIELD), isEmpty());
    }

    @Test
    public void generate_CurrentNotNull_FinalNull_ShouldReturnDeletedFieldChange() {
        when(auditFieldValueResolver.resolve(NAME_AUDITED_FIELD, currentState)).thenReturn(Triptional.of(OLD_NAME));
        when(auditFieldValueResolver.resolve(NAME_AUDITED_FIELD, finalState)).thenReturn(Triptional.nullInstance());

        when(auditFieldValueResolver.resolveToString(NAME_AUDITED_FIELD, currentState)).thenReturn(Triptional.of(OLD_NAME));
        when(auditFieldValueResolver.resolveToString(NAME_AUDITED_FIELD, finalState)).thenReturn(Triptional.nullInstance());

        assertThat(generate(NAME_AUDITED_FIELD),
                   isPresentAnd(is(FieldAuditRecord.builder(NAME_FIELD_NAME).oldValue(OLD_NAME).build())));
    }

    @Test
    public void generate_CurrentNull_FinalNotNull_ShouldReturnCreatedFieldChange() {
        when(auditFieldValueResolver.resolve(NAME_AUDITED_FIELD, currentState)).thenReturn(Triptional.nullInstance());
        when(auditFieldValueResolver.resolve(NAME_AUDITED_FIELD, finalState)).thenReturn(Triptional.of(NEW_NAME));

        when(auditFieldValueResolver.resolveToString(NAME_AUDITED_FIELD, currentState)).thenReturn(Triptional.nullInstance());
        when(auditFieldValueResolver.resolveToString(NAME_AUDITED_FIELD, finalState)).thenReturn(Triptional.of(NEW_NAME));

        assertThat(generate(NAME_AUDITED_FIELD),
                   isPresentAnd(is(FieldAuditRecord.builder(NAME_FIELD_NAME).newValue(NEW_NAME).build())));
    }

    @Test
    public void generate_CurrentNull_FinalNull_ShouldReturnEmpty() {
        when(auditFieldValueResolver.resolve(NAME_AUDITED_FIELD, currentState)).thenReturn(Triptional.nullInstance());
        when(auditFieldValueResolver.resolve(NAME_AUDITED_FIELD, finalState)).thenReturn(Triptional.nullInstance());

        assertThat(generate(NAME_AUDITED_FIELD), isEmpty());
    }

    @Test
    public void generate_CurrentAbsent_FinalNotNull_ShouldReturnCreatedFieldChange() {
        when(auditFieldValueResolver.resolve(NAME_AUDITED_FIELD, currentState)).thenReturn(Triptional.absent());
        when(auditFieldValueResolver.resolve(NAME_AUDITED_FIELD, finalState)).thenReturn(Triptional.of(NEW_NAME));

        when(auditFieldValueResolver.resolveToString(NAME_AUDITED_FIELD, currentState)).thenReturn(Triptional.absent());
        when(auditFieldValueResolver.resolveToString(NAME_AUDITED_FIELD, finalState)).thenReturn(Triptional.of(NEW_NAME));

        assertThat(generate(NAME_AUDITED_FIELD),
                   isPresentAnd(is(FieldAuditRecord.builder(NAME_FIELD_NAME).newValue(NEW_NAME).build())));
    }

    @Test
    public void generate_CurrentAbsent_FinalNull_ShouldReturnFieldChangeWithEmptyContents() {
        when(auditFieldValueResolver.resolve(NAME_AUDITED_FIELD, currentState)).thenReturn(Triptional.absent());
        when(auditFieldValueResolver.resolve(NAME_AUDITED_FIELD, finalState)).thenReturn(Triptional.nullInstance());

        when(auditFieldValueResolver.resolveToString(NAME_AUDITED_FIELD, currentState)).thenReturn(Triptional.absent());
        when(auditFieldValueResolver.resolveToString(NAME_AUDITED_FIELD, finalState)).thenReturn(Triptional.nullInstance());

        assertThat(generate(NAME_AUDITED_FIELD), isPresentAnd(is(FieldAuditRecord.builder(NAME_FIELD_NAME).build())));
    }

    @Test
    public void generate_CurrentAbsent_FinalAbsent_ShouldReturnEmpty() {
        when(auditFieldValueResolver.resolve(NAME_AUDITED_FIELD, currentState)).thenReturn(Triptional.absent());
        when(auditFieldValueResolver.resolve(NAME_AUDITED_FIELD, finalState)).thenReturn(Triptional.absent());

        assertThat(generate(NAME_AUDITED_FIELD), isEmpty());
    }

    private Optional<? extends FieldAuditRecord> generate(final AuditedField<AuditedType, ?> field) {
        return generator.generate(currentState, finalState, field);
    }
}