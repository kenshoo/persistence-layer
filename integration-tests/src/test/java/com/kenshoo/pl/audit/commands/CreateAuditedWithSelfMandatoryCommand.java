package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithSelfMandatoryType;

public class CreateAuditedWithSelfMandatoryCommand extends CreateEntityCommand<AuditedWithSelfMandatoryType>
    implements EntityCommandExt<AuditedWithSelfMandatoryType, CreateAuditedWithSelfMandatoryCommand> {

    public CreateAuditedWithSelfMandatoryCommand() {
        super(AuditedWithSelfMandatoryType.INSTANCE);
    }
}
