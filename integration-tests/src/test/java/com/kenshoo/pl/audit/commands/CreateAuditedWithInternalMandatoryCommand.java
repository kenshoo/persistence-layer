package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithInternalMandatoryType;

public class CreateAuditedWithInternalMandatoryCommand extends CreateEntityCommand<AuditedWithInternalMandatoryType>
    implements EntityCommandExt<AuditedWithInternalMandatoryType, CreateAuditedWithInternalMandatoryCommand> {

    public CreateAuditedWithInternalMandatoryCommand() {
        super(AuditedWithInternalMandatoryType.INSTANCE);
    }
}
