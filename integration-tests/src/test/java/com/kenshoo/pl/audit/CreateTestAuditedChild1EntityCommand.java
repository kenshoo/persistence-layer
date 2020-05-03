package com.kenshoo.pl.audit;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.internal.audit.TestAuditedChild1EntityType;

public class CreateTestAuditedChild1EntityCommand extends CreateEntityCommand<TestAuditedChild1EntityType> implements EntityCommandExt<TestAuditedChild1EntityType, CreateTestAuditedChild1EntityCommand> {

    public CreateTestAuditedChild1EntityCommand() {
        super(TestAuditedChild1EntityType.INSTANCE);
    }
}
