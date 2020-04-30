package com.kenshoo.pl.audit;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.internal.audit.TestAuditedEntityType;

public class CreateTestAuditedEntityCommand extends CreateEntityCommand<TestAuditedEntityType> implements EntityCommandExt<TestAuditedEntityType, CreateTestAuditedEntityCommand> {

    public CreateTestAuditedEntityCommand() {
        super(TestAuditedEntityType.INSTANCE);
    }
}
