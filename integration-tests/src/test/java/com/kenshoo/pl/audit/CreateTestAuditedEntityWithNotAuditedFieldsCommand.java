package com.kenshoo.pl.audit;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.internal.audit.TestAuditedEntityWithNotAuditedFieldsType;

public class CreateTestAuditedEntityWithNotAuditedFieldsCommand extends CreateEntityCommand<TestAuditedEntityWithNotAuditedFieldsType> implements EntityCommandExt<TestAuditedEntityWithNotAuditedFieldsType, CreateTestAuditedEntityWithNotAuditedFieldsCommand> {

    public CreateTestAuditedEntityWithNotAuditedFieldsCommand() {
        super(TestAuditedEntityWithNotAuditedFieldsType.INSTANCE);
    }
}
