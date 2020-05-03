package com.kenshoo.pl.audit;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.internal.audit.TestAuditedChild2EntityType;

public class CreateTestAuditedChild2EntityCommand extends CreateEntityCommand<TestAuditedChild2EntityType> implements EntityCommandExt<TestAuditedChild2EntityType, CreateTestAuditedChild2EntityCommand> {

    public CreateTestAuditedChild2EntityCommand() {
        super(TestAuditedChild2EntityType.INSTANCE);
    }
}
