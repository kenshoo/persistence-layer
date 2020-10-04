package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithAncestorMandatoryType;

public class UpdateAuditedWithAncestorMandatoryCommand extends UpdateEntityCommand<AuditedWithAncestorMandatoryType, Identifier<AuditedWithAncestorMandatoryType>>
    implements EntityCommandExt<AuditedWithAncestorMandatoryType, UpdateAuditedWithAncestorMandatoryCommand> {

    public UpdateAuditedWithAncestorMandatoryCommand(final long id) {
        super(AuditedWithAncestorMandatoryType.INSTANCE, new SingleUniqueKey<>(AuditedWithAncestorMandatoryType.ID).createIdentifier(id));
    }
}
