package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedChild2Type;

public class CreateAuditedChild2Command extends CreateEntityCommand<AuditedChild2Type> implements EntityCommandExt<AuditedChild2Type, CreateAuditedChild2Command> {

    public CreateAuditedChild2Command() {
        super(AuditedChild2Type.INSTANCE);
    }
}
