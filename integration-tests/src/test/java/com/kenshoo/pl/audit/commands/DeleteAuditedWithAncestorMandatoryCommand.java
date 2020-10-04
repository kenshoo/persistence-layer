package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithAncestorMandatoryType;

public class DeleteAuditedWithAncestorMandatoryCommand extends DeleteEntityCommand<AuditedWithAncestorMandatoryType, Identifier<AuditedWithAncestorMandatoryType>>
    implements EntityCommandExt<AuditedWithAncestorMandatoryType, DeleteAuditedWithAncestorMandatoryCommand> {

    public DeleteAuditedWithAncestorMandatoryCommand(final long id) {
        super(AuditedWithAncestorMandatoryType.INSTANCE, new SingleUniqueKey<>(AuditedWithAncestorMandatoryType.ID).createIdentifier(id));
    }
}
