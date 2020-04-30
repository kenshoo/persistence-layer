package com.kenshoo.pl.audit;

import com.kenshoo.pl.entity.DeleteEntityCommand;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.SingleUniqueKeyValue;
import com.kenshoo.pl.entity.internal.audit.TestAuditedEntityType;

public class DeleteTestAuditedEntityCommand extends DeleteEntityCommand<TestAuditedEntityType, Identifier<TestAuditedEntityType>> {

    public DeleteTestAuditedEntityCommand(final long id) {
        super(TestAuditedEntityType.INSTANCE, new SingleUniqueKeyValue<>(TestAuditedEntityType.ID, id));
    }
}
