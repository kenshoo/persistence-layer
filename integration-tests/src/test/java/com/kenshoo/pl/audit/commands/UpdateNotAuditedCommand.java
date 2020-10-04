package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedType;

public class UpdateNotAuditedCommand extends UpdateEntityCommand<NotAuditedType, Identifier<NotAuditedType>>
    implements EntityCommandExt<NotAuditedType, UpdateNotAuditedCommand> {

    public UpdateNotAuditedCommand(final long id) {
        super(NotAuditedType.INSTANCE, new SingleUniqueKey<>(NotAuditedType.ID).createIdentifier(id));
    }
}
