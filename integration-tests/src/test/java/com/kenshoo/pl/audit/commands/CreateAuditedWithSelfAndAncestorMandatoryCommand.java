package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithSelfAndAncestorMandatoryType;

public class CreateAuditedWithSelfAndAncestorMandatoryCommand extends CreateEntityCommand<AuditedWithSelfAndAncestorMandatoryType>
    implements EntityCommandExt<AuditedWithSelfAndAncestorMandatoryType, CreateAuditedWithSelfAndAncestorMandatoryCommand> {

    public CreateAuditedWithSelfAndAncestorMandatoryCommand() {
        super(AuditedWithSelfAndAncestorMandatoryType.INSTANCE);
    }
}
