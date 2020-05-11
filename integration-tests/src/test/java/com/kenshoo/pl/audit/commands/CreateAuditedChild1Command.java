package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedChild1Type;

public class CreateAuditedChild1Command extends CreateEntityCommand<AuditedChild1Type> implements EntityCommandExt<AuditedChild1Type, CreateAuditedChild1Command> {

    public CreateAuditedChild1Command() {
        super(AuditedChild1Type.INSTANCE);
    }
}
