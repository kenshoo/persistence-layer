package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedAutoIncIdChild1Type;

public class CreateAuditedAutoIncChild1Command extends CreateEntityCommand<AuditedAutoIncIdChild1Type> implements EntityCommandExt<AuditedAutoIncIdChild1Type, CreateAuditedAutoIncChild1Command> {

    public CreateAuditedAutoIncChild1Command() {
        super(AuditedAutoIncIdChild1Type.INSTANCE);
    }
}
