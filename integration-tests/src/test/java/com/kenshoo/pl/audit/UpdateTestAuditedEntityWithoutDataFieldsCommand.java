package com.kenshoo.pl.audit;

import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.SingleUniqueKeyValue;
import com.kenshoo.pl.entity.UpdateEntityCommand;
import com.kenshoo.pl.entity.internal.audit.TestAuditedEntityWithoutDataFieldsType;

public class UpdateTestAuditedEntityWithoutDataFieldsCommand extends UpdateEntityCommand<TestAuditedEntityWithoutDataFieldsType, Identifier<TestAuditedEntityWithoutDataFieldsType>>
    implements EntityCommandExt<TestAuditedEntityWithoutDataFieldsType, UpdateTestAuditedEntityWithoutDataFieldsCommand> {

    public UpdateTestAuditedEntityWithoutDataFieldsCommand(final long id) {
        super(TestAuditedEntityWithoutDataFieldsType.INSTANCE, new SingleUniqueKeyValue<>(TestAuditedEntityWithoutDataFieldsType.ID, id));
    }
}
