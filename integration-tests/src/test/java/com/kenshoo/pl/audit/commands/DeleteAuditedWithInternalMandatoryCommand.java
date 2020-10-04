package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithInternalMandatoryType;

public class DeleteAuditedWithInternalMandatoryCommand extends DeleteEntityCommand<AuditedWithInternalMandatoryType, Identifier<AuditedWithInternalMandatoryType>>
    implements EntityCommandExt<AuditedWithInternalMandatoryType, DeleteAuditedWithInternalMandatoryCommand> {

    public DeleteAuditedWithInternalMandatoryCommand(final long id) {
        super(AuditedWithInternalMandatoryType.INSTANCE, new SingleUniqueKey<>(AuditedWithInternalMandatoryType.ID).createIdentifier(id));
    }
}
