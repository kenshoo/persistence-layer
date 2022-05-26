package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedManualIdType;

public class CreateAuditedManualIdTypeCommand extends CreateEntityCommand<AuditedManualIdType>
    implements EntityCommandExt<AuditedManualIdType, CreateAuditedManualIdTypeCommand> {

    public CreateAuditedManualIdTypeCommand() {
        super(AuditedManualIdType.INSTANCE);
    }
}
