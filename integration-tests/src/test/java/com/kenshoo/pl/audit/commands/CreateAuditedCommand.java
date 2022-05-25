package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedAutoIncIdType;

public class CreateAuditedCommand extends CreateEntityCommand<AuditedAutoIncIdType>
    implements EntityCommandExt<AuditedAutoIncIdType, CreateAuditedCommand> {

    public CreateAuditedCommand() {
        super(AuditedAutoIncIdType.INSTANCE);
    }
}
