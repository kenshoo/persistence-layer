package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedAutoIncIdChild2Type;

public class CreateAuditedChild2Command extends CreateEntityCommand<AuditedAutoIncIdChild2Type> implements EntityCommandExt<AuditedAutoIncIdChild2Type, CreateAuditedChild2Command> {

    public CreateAuditedChild2Command() {
        super(AuditedAutoIncIdChild2Type.INSTANCE);
    }
}
