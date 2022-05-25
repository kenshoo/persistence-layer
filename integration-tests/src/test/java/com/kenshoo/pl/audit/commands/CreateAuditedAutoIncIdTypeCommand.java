package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedAutoIncIdType;

public class CreateAuditedAutoIncIdTypeCommand extends CreateEntityCommand<AuditedAutoIncIdType>
    implements EntityCommandExt<AuditedAutoIncIdType, CreateAuditedAutoIncIdTypeCommand> {

    public CreateAuditedAutoIncIdTypeCommand() {
        super(AuditedAutoIncIdType.INSTANCE);
    }
}
