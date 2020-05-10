package com.kenshoo.pl.audit;

import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.SingleUniqueKeyValue;
import com.kenshoo.pl.entity.UpdateEntityCommand;
import com.kenshoo.pl.entity.internal.audit.TestAuditedChild1EntityType;

public class UpdateTestAuditedChild1EntityCommand extends UpdateEntityCommand<TestAuditedChild1EntityType, Identifier<TestAuditedChild1EntityType>>
    implements EntityCommandExt<TestAuditedChild1EntityType, UpdateTestAuditedChild1EntityCommand> {

    public UpdateTestAuditedChild1EntityCommand(final long id) {
        super(TestAuditedChild1EntityType.INSTANCE, new SingleUniqueKeyValue<>(TestAuditedChild1EntityType.ID, id));
    }
}
