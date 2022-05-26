package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedChildWithoutIdOfParentWithoutIdType;

public class CreateAuditedChildWithoutIdOfParentWithoutIdCommand extends CreateEntityCommand<AuditedChildWithoutIdOfParentWithoutIdType> implements EntityCommandExt<AuditedChildWithoutIdOfParentWithoutIdType, CreateAuditedChildWithoutIdOfParentWithoutIdCommand> {

    public CreateAuditedChildWithoutIdOfParentWithoutIdCommand() {
        super(AuditedChildWithoutIdOfParentWithoutIdType.INSTANCE);
    }
}
