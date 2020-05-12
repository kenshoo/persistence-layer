package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.internal.audit.entitytypes.InclusiveAuditedType;

public class CreateInclusiveAuditedCommand extends CreateEntityCommand<InclusiveAuditedType> implements EntityCommandExt<InclusiveAuditedType, CreateInclusiveAuditedCommand> {

    public CreateInclusiveAuditedCommand() {
        super(InclusiveAuditedType.INSTANCE);
    }
}
