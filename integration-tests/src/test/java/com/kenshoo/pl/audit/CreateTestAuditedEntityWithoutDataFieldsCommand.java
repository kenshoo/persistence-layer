package com.kenshoo.pl.audit;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.internal.audit.TestAuditedEntityWithoutDataFieldsType;

public class CreateTestAuditedEntityWithoutDataFieldsCommand extends CreateEntityCommand<TestAuditedEntityWithoutDataFieldsType>
    implements EntityCommandExt<TestAuditedEntityWithoutDataFieldsType, CreateTestAuditedEntityWithoutDataFieldsCommand> {

    public CreateTestAuditedEntityWithoutDataFieldsCommand(final long id) {
        super(TestAuditedEntityWithoutDataFieldsType.INSTANCE);
        set(TestAuditedEntityWithoutDataFieldsType.ID, id);
    }
}
