package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithAncestorFieldNameOverridesType;

public class CreateAuditedWithAncestorFieldNameOverridesCommand extends CreateEntityCommand<AuditedWithAncestorFieldNameOverridesType>
    implements EntityCommandExt<AuditedWithAncestorFieldNameOverridesType, CreateAuditedWithAncestorFieldNameOverridesCommand> {

    public CreateAuditedWithAncestorFieldNameOverridesCommand() {
        super(AuditedWithAncestorFieldNameOverridesType.INSTANCE);
    }
}
