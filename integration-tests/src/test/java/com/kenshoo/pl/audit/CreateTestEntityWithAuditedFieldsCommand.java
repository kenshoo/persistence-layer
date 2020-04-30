package com.kenshoo.pl.audit;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.internal.audit.TestEntityWithAuditedFieldsType;

public class CreateTestEntityWithAuditedFieldsCommand extends CreateEntityCommand<TestEntityWithAuditedFieldsType> implements EntityCommandExt<TestEntityWithAuditedFieldsType, CreateTestEntityWithAuditedFieldsCommand> {

    public CreateTestEntityWithAuditedFieldsCommand() {
        super(TestEntityWithAuditedFieldsType.INSTANCE);
    }
}
