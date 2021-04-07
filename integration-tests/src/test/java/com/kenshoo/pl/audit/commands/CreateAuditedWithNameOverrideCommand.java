package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithNameOverrideType;

public class CreateAuditedWithNameOverrideCommand extends CreateEntityCommand<AuditedWithNameOverrideType>
    implements EntityCommandExt<AuditedWithNameOverrideType, CreateAuditedWithNameOverrideCommand> {

    public CreateAuditedWithNameOverrideCommand() {
        super(AuditedWithNameOverrideType.INSTANCE);
    }
}
