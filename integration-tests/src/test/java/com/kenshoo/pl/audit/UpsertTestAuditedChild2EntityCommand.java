package com.kenshoo.pl.audit;

import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.InsertOnDuplicateUpdateCommand;
import com.kenshoo.pl.entity.SingleUniqueKeyValue;
import com.kenshoo.pl.entity.internal.audit.TestAuditedChild2EntityType;

public class UpsertTestAuditedChild2EntityCommand extends InsertOnDuplicateUpdateCommand<TestAuditedChild2EntityType, Identifier<TestAuditedChild2EntityType>>
    implements EntityCommandExt<TestAuditedChild2EntityType, UpsertTestAuditedChild2EntityCommand> {

    public UpsertTestAuditedChild2EntityCommand(final String name) {
        super(TestAuditedChild2EntityType.INSTANCE, new SingleUniqueKeyValue<>(TestAuditedChild2EntityType.NAME, name));
    }
}
