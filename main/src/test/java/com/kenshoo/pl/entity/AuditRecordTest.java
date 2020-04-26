package com.kenshoo.pl.entity;

import com.kenshoo.pl.entity.internal.audit.TestAuditedEntityType;
import org.junit.Test;
import org.mockito.Mock;

import static com.kenshoo.pl.entity.ChangeOperation.CREATE;
import static java.util.Collections.singleton;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AuditRecordTest {

    private static final String ENTITY_ID = "123";
    private static final FieldAuditRecord<TestAuditedEntityType> NAME_FIELD_RECORD =
        new FieldAuditRecord<>(TestAuditedEntityType.NAME, null, "name");

    @Mock
    private AuditRecord<?> childRecord;

    @Test
    public void hasNoChanges_WithFieldRecords_WithChildRecords_ShouldReturnFalse() {
        final AuditRecord<TestAuditedEntityType> auditRecord =
            new AuditRecord.Builder<TestAuditedEntityType>()
                .withEntityType(TestAuditedEntityType.INSTANCE)
                .withEntityId(ENTITY_ID)
                .withOperator(CREATE)
                .withFieldRecords(singleton(NAME_FIELD_RECORD))
                .withChildRecords(singleton(childRecord))
                .build();

        assertThat(auditRecord.hasNoChanges(), is(false));
    }

    @Test
    public void hasNoChanges_WithFieldRecords_WithoutChildRecords_ShouldReturnFalse() {
        final AuditRecord<TestAuditedEntityType> auditRecord =
            new AuditRecord.Builder<TestAuditedEntityType>()
                .withEntityType(TestAuditedEntityType.INSTANCE)
                .withEntityId(ENTITY_ID)
                .withOperator(CREATE)
                .withFieldRecords(singleton(NAME_FIELD_RECORD))
                .build();

        assertThat(auditRecord.hasNoChanges(), is(false));
    }

    @Test
    public void hasNoChanges_WithoutFieldRecords_WithChildRecords_ShouldReturnFalse() {
        final AuditRecord<TestAuditedEntityType> auditRecord =
            new AuditRecord.Builder<TestAuditedEntityType>()
                .withEntityType(TestAuditedEntityType.INSTANCE)
                .withEntityId(ENTITY_ID)
                .withOperator(CREATE)
                .withChildRecords(singleton(childRecord))
                .build();

        assertThat(auditRecord.hasNoChanges(), is(false));
    }

    @Test
    public void hasNoChanges_WithoutFieldRecords_WithoutChildRecords_ShouldReturnTrue() {
        final AuditRecord<TestAuditedEntityType> auditRecord =
            new AuditRecord.Builder<TestAuditedEntityType>()
                .withEntityType(TestAuditedEntityType.INSTANCE)
                .withEntityId(ENTITY_ID)
                .withOperator(CREATE)
                .build();

        assertThat(auditRecord.hasNoChanges(), is(true));
    }
}