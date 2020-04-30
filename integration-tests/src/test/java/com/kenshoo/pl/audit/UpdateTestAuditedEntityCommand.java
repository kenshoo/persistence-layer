package com.kenshoo.pl.audit;

import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.SingleUniqueKeyValue;
import com.kenshoo.pl.entity.UpdateEntityCommand;
import com.kenshoo.pl.entity.internal.audit.TestAuditedEntityType;

public class UpdateTestAuditedEntityCommand extends UpdateEntityCommand<TestAuditedEntityType, Identifier<TestAuditedEntityType>> implements EntityCommandExt<TestAuditedEntityType, UpdateTestAuditedEntityCommand> {

    public UpdateTestAuditedEntityCommand(final long id) {
        super(TestAuditedEntityType.INSTANCE, new SingleUniqueKeyValue<>(TestAuditedEntityType.ID, id));
    }
}
