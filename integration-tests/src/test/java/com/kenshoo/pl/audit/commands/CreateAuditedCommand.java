package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType;

public class CreateAuditedCommand extends CreateEntityCommand<AuditedType>
    implements EntityCommandExt<AuditedType, CreateAuditedCommand> {

    public CreateAuditedCommand() {
        super(AuditedType.INSTANCE);
    }
}
