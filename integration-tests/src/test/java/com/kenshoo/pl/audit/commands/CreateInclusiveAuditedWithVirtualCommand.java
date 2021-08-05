package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.internal.audit.entitytypes.InclusiveAuditedWithVirtualType;

public class CreateInclusiveAuditedWithVirtualCommand extends CreateEntityCommand<InclusiveAuditedWithVirtualType>
    implements EntityCommandExt<InclusiveAuditedWithVirtualType, CreateInclusiveAuditedWithVirtualCommand> {

    public CreateInclusiveAuditedWithVirtualCommand() {
        super(InclusiveAuditedWithVirtualType.INSTANCE);
    }
}
