package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedChildManualIdType;

public class CreateAuditedChildManualIdCommand extends CreateEntityCommand<AuditedChildManualIdType> implements EntityCommandExt<AuditedChildManualIdType, CreateAuditedChildManualIdCommand> {

    public CreateAuditedChildManualIdCommand() {
        super(AuditedChildManualIdType.INSTANCE);
    }
}
