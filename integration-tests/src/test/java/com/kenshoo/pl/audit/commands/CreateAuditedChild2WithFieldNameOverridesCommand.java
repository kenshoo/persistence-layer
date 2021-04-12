package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedChild2WithFieldNameOverridesType;

public class CreateAuditedChild2WithFieldNameOverridesCommand extends CreateEntityCommand<AuditedChild2WithFieldNameOverridesType>
    implements EntityCommandExt<AuditedChild2WithFieldNameOverridesType, CreateAuditedChild2WithFieldNameOverridesCommand> {

    public CreateAuditedChild2WithFieldNameOverridesCommand() {
        super(AuditedChild2WithFieldNameOverridesType.INSTANCE);
    }
}
