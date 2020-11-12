package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithInternalAndAncestorMandatoryType;

public class CreateAuditedWithInternalAndAncestorMandatoryCommand extends CreateEntityCommand<AuditedWithInternalAndAncestorMandatoryType>
    implements EntityCommandExt<AuditedWithInternalAndAncestorMandatoryType, CreateAuditedWithInternalAndAncestorMandatoryCommand> {

    public CreateAuditedWithInternalAndAncestorMandatoryCommand() {
        super(AuditedWithInternalAndAncestorMandatoryType.INSTANCE);
    }
}
