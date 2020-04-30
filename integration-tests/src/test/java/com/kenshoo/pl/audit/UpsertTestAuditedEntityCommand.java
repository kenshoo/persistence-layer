package com.kenshoo.pl.audit;

import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.InsertOnDuplicateUpdateCommand;
import com.kenshoo.pl.entity.SingleUniqueKeyValue;
import com.kenshoo.pl.entity.internal.audit.TestAuditedEntityType;

public class UpsertTestAuditedEntityCommand extends InsertOnDuplicateUpdateCommand<TestAuditedEntityType, Identifier<TestAuditedEntityType>> implements EntityCommandExt<TestAuditedEntityType, UpsertTestAuditedEntityCommand> {

    public UpsertTestAuditedEntityCommand(final String name) {
        super(TestAuditedEntityType.INSTANCE, new SingleUniqueKeyValue<>(TestAuditedEntityType.NAME, name));
    }
}
