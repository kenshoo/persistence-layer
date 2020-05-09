package com.kenshoo.pl.audit;

import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.SingleUniqueKeyValue;
import com.kenshoo.pl.entity.UpdateEntityCommand;
import com.kenshoo.pl.entity.internal.audit.TestAuditedChild2EntityType;

public class UpdateTestAuditedChild2EntityCommand extends UpdateEntityCommand<TestAuditedChild2EntityType, Identifier<TestAuditedChild2EntityType>>
    implements EntityCommandExt<TestAuditedChild2EntityType, UpdateTestAuditedChild2EntityCommand> {

    public UpdateTestAuditedChild2EntityCommand(final long id) {
        super(TestAuditedChild2EntityType.INSTANCE, new SingleUniqueKeyValue<>(TestAuditedChild2EntityType.ID, id));
    }
}
