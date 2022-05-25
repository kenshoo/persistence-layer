package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedAutoIncIdChild1Type;

public class CreateAuditedChild1Command extends CreateEntityCommand<AuditedAutoIncIdChild1Type> implements EntityCommandExt<AuditedAutoIncIdChild1Type, CreateAuditedChild1Command> {

    public CreateAuditedChild1Command() {
        super(AuditedAutoIncIdChild1Type.INSTANCE);
    }
}
