package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithAncestorMandatoryType;

public class CreateAuditedWithAncestorMandatoryCommand extends CreateEntityCommand<AuditedWithAncestorMandatoryType>
    implements EntityCommandExt<AuditedWithAncestorMandatoryType, CreateAuditedWithAncestorMandatoryCommand> {

    public CreateAuditedWithAncestorMandatoryCommand() {
        super(AuditedWithAncestorMandatoryType.INSTANCE);
    }
}
