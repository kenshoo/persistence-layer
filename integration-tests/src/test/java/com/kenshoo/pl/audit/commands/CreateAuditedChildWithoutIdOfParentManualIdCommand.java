package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedChildWithoutIdOfParentManualIdType;

public class CreateAuditedChildWithoutIdOfParentManualIdCommand extends CreateEntityCommand<AuditedChildWithoutIdOfParentManualIdType> implements EntityCommandExt<AuditedChildWithoutIdOfParentManualIdType, CreateAuditedChildWithoutIdOfParentManualIdCommand> {

    public CreateAuditedChildWithoutIdOfParentManualIdCommand() {
        super(AuditedChildWithoutIdOfParentManualIdType.INSTANCE);
    }
}
