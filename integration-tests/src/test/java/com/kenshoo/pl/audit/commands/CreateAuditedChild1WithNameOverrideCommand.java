package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedChild1WithNameOverrideType;

public class CreateAuditedChild1WithNameOverrideCommand extends CreateEntityCommand<AuditedChild1WithNameOverrideType>
    implements EntityCommandExt<AuditedChild1WithNameOverrideType, CreateAuditedChild1WithNameOverrideCommand> {

    public CreateAuditedChild1WithNameOverrideCommand() {
        super(AuditedChild1WithNameOverrideType.INSTANCE);
    }
}
