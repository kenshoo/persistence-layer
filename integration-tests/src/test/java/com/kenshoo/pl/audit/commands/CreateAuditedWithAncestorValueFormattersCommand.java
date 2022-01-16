package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithAncestorValueFormattersType;

public class CreateAuditedWithAncestorValueFormattersCommand
    extends CreateEntityCommand<AuditedWithAncestorValueFormattersType>
    implements EntityCommandExt<AuditedWithAncestorValueFormattersType, CreateAuditedWithAncestorValueFormattersCommand> {

    public CreateAuditedWithAncestorValueFormattersCommand() {
        super(AuditedWithAncestorValueFormattersType.INSTANCE);
    }
}
