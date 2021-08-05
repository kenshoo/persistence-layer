package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithVirtualType;

public class CreateAuditedWithVirtualCommand extends CreateEntityCommand<AuditedWithVirtualType>
    implements EntityCommandExt<AuditedWithVirtualType, CreateAuditedWithVirtualCommand> {

    public CreateAuditedWithVirtualCommand() {
        super(AuditedWithVirtualType.INSTANCE);
    }
}
