package com.kenshoo.pl.audit;

import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.InsertOnDuplicateUpdateCommand;
import com.kenshoo.pl.entity.SingleUniqueKeyValue;
import com.kenshoo.pl.entity.internal.audit.TestAuditedChild1EntityType;

public class UpsertTestAuditedChild1EntityCommand extends InsertOnDuplicateUpdateCommand<TestAuditedChild1EntityType, Identifier<TestAuditedChild1EntityType>>
    implements EntityCommandExt<TestAuditedChild1EntityType, UpsertTestAuditedChild1EntityCommand> {

    public UpsertTestAuditedChild1EntityCommand(final String name) {
        super(TestAuditedChild1EntityType.INSTANCE, new SingleUniqueKeyValue<>(TestAuditedChild1EntityType.NAME, name));
    }
}
