package com.kenshoo.pl.entity.internal.audit;

import com.google.common.collect.ImmutableList;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.audit.AuditRecord;
import com.kenshoo.pl.entity.internal.EntityIdExtractor;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedAncestorType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAnd;
import static com.kenshoo.pl.entity.ChangeOperation.DELETE;
import static com.kenshoo.pl.entity.matchers.audit.AuditRecordMatchers.*;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuditRecordGeneratorImplForDeleteTest {

    private static final String ENTITY_TYPE_NAME = "someEntityType";
    private static final long ID = 1234;
    private static final String STRING_ID = String.valueOf(ID);
    private static final String ANCESTOR_NAME = "ancestorName";
    private static final String ANCESTOR_DESC = "ancestorDesc";

    @Mock
    private AuditMandatoryFieldValuesGenerator mandatoryFieldValuesGenerator;

    @Mock
    private AuditFieldChangesGenerator<AuditedType> fieldChangesGenerator;

    @Mock
    private EntityIdExtractor entityIdExtractor;

    @Mock
    private ChangeContext changeContext;

    @Mock
    private CurrentEntityState currentState;

    @Mock
    private EntityChange<AuditedType> cmd;

    @Mock
    private FinalEntityState finalState;

    private AuditRecordGeneratorImpl<AuditedType> auditRecordGenerator;

    @Before
    public void setUp() {
        when(cmd.getChangeOperation()).thenReturn(DELETE);
        when(changeContext.getEntity(cmd)).thenReturn(currentState);
        when(changeContext.getFinalEntity(cmd)).thenReturn(finalState);
        when(entityIdExtractor.extract(cmd, currentState)).thenReturn(Optional.of(STRING_ID));
        when(fieldChangesGenerator.generate(currentState, finalState)).thenReturn(emptyList());

        auditRecordGenerator = new AuditRecordGeneratorImpl<>(mandatoryFieldValuesGenerator,
                                                              fieldChangesGenerator,
                                                              entityIdExtractor,
                                                              ENTITY_TYPE_NAME);
    }

    @Test
    public void generate_WithIdOnly_ShouldGenerateBasicData() {
        when(mandatoryFieldValuesGenerator.generate(finalState)).thenReturn(emptyList());

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, changeContext, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasEntityType(ENTITY_TYPE_NAME),
                                      hasEntityId(STRING_ID),
                                      hasOperator(DELETE))));
    }

    @Test(expected = IllegalStateException.class)
    public void generate_WithNoId_ShouldThrowException() {
        when(entityIdExtractor.extract(cmd, currentState)).thenReturn(Optional.empty());

        auditRecordGenerator.generate(cmd, changeContext, emptyList());
    }

    @Test
    public void generate_WithMandatoryOnly_ShouldGenerateMandatoryFieldValues() {
        final Collection<EntityFieldValue> expectedMandatoryFieldValues =
            ImmutableList.of(new EntityFieldValue(NotAuditedAncestorType.NAME, ANCESTOR_NAME),
                             new EntityFieldValue(NotAuditedAncestorType.DESC, ANCESTOR_DESC));

        when(mandatoryFieldValuesGenerator.generate(finalState)).thenReturn(expectedMandatoryFieldValues);

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, changeContext, emptyList());

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasMandatoryFieldValue(NotAuditedAncestorType.NAME, ANCESTOR_NAME),
                                      hasMandatoryFieldValue(NotAuditedAncestorType.DESC, ANCESTOR_DESC))));
    }

    @Test
    public void generate_WithChildRecordsOnly_ShouldGenerateChildRecords() {
        when(mandatoryFieldValuesGenerator.generate(finalState)).thenReturn(emptyList());

        final List<AuditRecord<?>> childRecords = ImmutableList.of(mockChildRecord(), mockChildRecord());

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, changeContext, childRecords);

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasSameChildRecord(childRecords.get(0)),
                                      hasSameChildRecord(childRecords.get(1)))));
    }

    @Test
    public void generate_WithMandatoryAndChildRecords_ShouldGenerateMandatoryFieldValuesAndChildRecords() {
        final Collection<EntityFieldValue> expectedMandatoryFieldValues =
            ImmutableList.of(new EntityFieldValue(NotAuditedAncestorType.NAME, ANCESTOR_NAME),
                             new EntityFieldValue(NotAuditedAncestorType.DESC, ANCESTOR_DESC));

        when(mandatoryFieldValuesGenerator.generate(finalState)).thenReturn(expectedMandatoryFieldValues);

        final List<AuditRecord<?>> childRecords = ImmutableList.of(mockChildRecord(), mockChildRecord());

        final Optional<? extends AuditRecord<AuditedType>> actualOptionalAuditRecord =
            auditRecordGenerator.generate(cmd, changeContext, childRecords);

        assertThat(actualOptionalAuditRecord,
                   isPresentAnd(allOf(hasMandatoryFieldValue(NotAuditedAncestorType.NAME, ANCESTOR_NAME),
                                      hasMandatoryFieldValue(NotAuditedAncestorType.DESC, ANCESTOR_DESC),
                                      hasSameChildRecord(childRecords.get(0)),
                                      hasSameChildRecord(childRecords.get(1)))));
    }

    private AuditRecord<?> mockChildRecord() {
        return mock(AuditRecord.class);
    }
}