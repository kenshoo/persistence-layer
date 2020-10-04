package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType;

public class DeleteAuditedCommand extends DeleteEntityCommand<AuditedType, Identifier<AuditedType>>
    implements EntityCommandExt<AuditedType, DeleteAuditedCommand> {

    public DeleteAuditedCommand(final long id) {
        super(AuditedType.INSTANCE, new SingleUniqueKey<>(AuditedType.ID).createIdentifier(id));
    }
}
