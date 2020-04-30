package com.kenshoo.pl.audit;

import com.kenshoo.pl.entity.DeleteEntityCommand;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.SingleUniqueKeyValue;
import com.kenshoo.pl.entity.internal.audit.TestAuditedEntityWithoutDataFieldsType;

public class DeleteTestAuditedEntityWithoutDataFieldsCommand extends DeleteEntityCommand<TestAuditedEntityWithoutDataFieldsType, Identifier<TestAuditedEntityWithoutDataFieldsType>> {

    public DeleteTestAuditedEntityWithoutDataFieldsCommand(final long id) {
        super(TestAuditedEntityWithoutDataFieldsType.INSTANCE, new SingleUniqueKeyValue<>(TestAuditedEntityWithoutDataFieldsType.ID, id));
    }
}
