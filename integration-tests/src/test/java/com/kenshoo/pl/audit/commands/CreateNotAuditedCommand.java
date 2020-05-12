package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedType;

public class CreateNotAuditedCommand extends CreateEntityCommand<NotAuditedType> implements EntityCommandExt<NotAuditedType, CreateNotAuditedCommand> {

    public CreateNotAuditedCommand() {
        super(NotAuditedType.INSTANCE);
    }
}
