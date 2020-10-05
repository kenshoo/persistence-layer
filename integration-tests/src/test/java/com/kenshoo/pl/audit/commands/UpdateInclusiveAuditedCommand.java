package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.audit.entitytypes.InclusiveAuditedType;

import static com.kenshoo.pl.entity.IdentifierType.uniqueKey;

public class UpdateInclusiveAuditedCommand extends UpdateEntityCommand<InclusiveAuditedType, Identifier<InclusiveAuditedType>> implements EntityCommandExt<InclusiveAuditedType, UpdateInclusiveAuditedCommand> {

    public UpdateInclusiveAuditedCommand(final long id) {
        super(InclusiveAuditedType.INSTANCE, uniqueKey(InclusiveAuditedType.ID).createIdentifier(id));
    }
}
