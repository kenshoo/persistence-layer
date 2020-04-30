package com.kenshoo.pl.audit;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.internal.audit.TestAuditedEntityWithoutDataFieldsType;

public class CreateTestAuditedEntityWithoutDataFieldsCommand extends CreateEntityCommand<TestAuditedEntityWithoutDataFieldsType> {

    public CreateTestAuditedEntityWithoutDataFieldsCommand(final long id) {
        super(TestAuditedEntityWithoutDataFieldsType.INSTANCE);
        set(TestAuditedEntityWithoutDataFieldsType.ID, id);
    }
}
