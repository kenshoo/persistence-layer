package com.kenshoo.pl.audit;

import com.kenshoo.pl.entity.DeleteEntityCommand;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.SingleUniqueKeyValue;
import com.kenshoo.pl.entity.internal.audit.TestAuditedChild2EntityType;

public class DeleteTestAuditedChild2EntityCommand extends DeleteEntityCommand<TestAuditedChild2EntityType, Identifier<TestAuditedChild2EntityType>> {

    public DeleteTestAuditedChild2EntityCommand(final long id) {
        super(TestAuditedChild2EntityType.INSTANCE, new SingleUniqueKeyValue<>(TestAuditedChild2EntityType.ID, id));
    }
}
