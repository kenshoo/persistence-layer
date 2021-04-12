package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithFieldNameOverridesType;

public class CreateAuditedWithFieldNameOverridesCommand extends CreateEntityCommand<AuditedWithFieldNameOverridesType>
    implements EntityCommandExt<AuditedWithFieldNameOverridesType, CreateAuditedWithFieldNameOverridesCommand> {

    public CreateAuditedWithFieldNameOverridesCommand() {
        super(AuditedWithFieldNameOverridesType.INSTANCE);
    }
}
