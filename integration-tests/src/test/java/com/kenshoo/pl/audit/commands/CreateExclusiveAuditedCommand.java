package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.internal.audit.entitytypes.ExclusiveAuditedType;

public class CreateExclusiveAuditedCommand extends CreateEntityCommand<ExclusiveAuditedType> implements EntityCommandExt<ExclusiveAuditedType, CreateExclusiveAuditedCommand> {

    public CreateExclusiveAuditedCommand() {
        super(ExclusiveAuditedType.INSTANCE);
    }
}
