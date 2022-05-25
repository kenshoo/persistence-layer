package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithoutIdType;

public class CreateAuditedWithoutIdTypeCommand extends CreateEntityCommand<AuditedWithoutIdType>
    implements EntityCommandExt<AuditedWithoutIdType, CreateAuditedWithoutIdTypeCommand> {

    public CreateAuditedWithoutIdTypeCommand() {
        super(AuditedWithoutIdType.INSTANCE);
    }
}
