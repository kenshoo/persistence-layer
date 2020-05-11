package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedChildType;

public class CreateNotAuditedChildCommand extends CreateEntityCommand<NotAuditedChildType> implements EntityCommandExt<NotAuditedChildType, CreateNotAuditedChildCommand> {

    public CreateNotAuditedChildCommand() {
        super(NotAuditedChildType.INSTANCE);
    }
}
