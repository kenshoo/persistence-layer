package com.kenshoo.pl.audit;

import com.kenshoo.pl.entity.DeleteEntityCommand;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.SingleUniqueKeyValue;
import com.kenshoo.pl.entity.internal.audit.TestAuditedChild1EntityType;

public class DeleteTestAuditedChild1EntityCommand extends DeleteEntityCommand<TestAuditedChild1EntityType, Identifier<TestAuditedChild1EntityType>> {

    public DeleteTestAuditedChild1EntityCommand(final long id) {
        super(TestAuditedChild1EntityType.INSTANCE, new SingleUniqueKeyValue<>(TestAuditedChild1EntityType.ID, id));
    }
}
