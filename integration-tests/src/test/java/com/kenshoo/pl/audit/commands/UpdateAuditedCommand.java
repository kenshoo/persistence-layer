package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType;

public class UpdateAuditedCommand extends UpdateEntityCommand<AuditedType, Identifier<AuditedType>> implements EntityCommandExt<AuditedType, UpdateAuditedCommand> {

    public UpdateAuditedCommand(final long id) {
        super(AuditedType.INSTANCE, new SingleUniqueKey<>(AuditedType.ID).createIdentifier(id));
    }
}
