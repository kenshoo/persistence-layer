package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedChild2WithNameOverrideType;

public class CreateAuditedChild2WithNameOverrideCommand extends CreateEntityCommand<AuditedChild2WithNameOverrideType>
    implements EntityCommandExt<AuditedChild2WithNameOverrideType, CreateAuditedChild2WithNameOverrideCommand> {

    public CreateAuditedChild2WithNameOverrideCommand() {
        super(AuditedChild2WithNameOverrideType.INSTANCE);
    }
}
