package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedChild1WithFieldNameOverridesType;

public class CreateAuditedChild1WithFieldNameOverridesCommand extends CreateEntityCommand<AuditedChild1WithFieldNameOverridesType>
    implements EntityCommandExt<AuditedChild1WithFieldNameOverridesType, CreateAuditedChild1WithFieldNameOverridesCommand> {

    public CreateAuditedChild1WithFieldNameOverridesCommand() {
        super(AuditedChild1WithFieldNameOverridesType.INSTANCE);
    }
}
