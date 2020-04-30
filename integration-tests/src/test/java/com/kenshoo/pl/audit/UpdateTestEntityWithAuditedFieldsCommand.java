package com.kenshoo.pl.audit;

import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.SingleUniqueKeyValue;
import com.kenshoo.pl.entity.UpdateEntityCommand;
import com.kenshoo.pl.entity.internal.audit.TestEntityWithAuditedFieldsType;

public class UpdateTestEntityWithAuditedFieldsCommand extends UpdateEntityCommand<TestEntityWithAuditedFieldsType, Identifier<TestEntityWithAuditedFieldsType>> implements EntityCommandExt<TestEntityWithAuditedFieldsType, UpdateTestEntityWithAuditedFieldsCommand> {

    public UpdateTestEntityWithAuditedFieldsCommand(final long id) {
        super(TestEntityWithAuditedFieldsType.INSTANCE, new SingleUniqueKeyValue<>(TestEntityWithAuditedFieldsType.ID, id));
    }
}
