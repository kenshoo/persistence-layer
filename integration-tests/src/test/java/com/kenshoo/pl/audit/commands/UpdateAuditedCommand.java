package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType;

import static com.kenshoo.pl.entity.IdentifierType.uniqueKey;

public class UpdateAuditedCommand extends UpdateEntityCommand<AuditedType, Identifier<AuditedType>> implements EntityCommandExt<AuditedType, UpdateAuditedCommand> {

    public UpdateAuditedCommand(final long id) {
        super(AuditedType.INSTANCE, uniqueKey(AuditedType.ID).createIdentifier(id));
    }
}
