package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedType;

public class DeleteNotAuditedCommand extends DeleteEntityCommand<NotAuditedType, Identifier<NotAuditedType>>
    implements EntityCommandExt<NotAuditedType, DeleteNotAuditedCommand> {

    public DeleteNotAuditedCommand(final long id) {
        super(NotAuditedType.INSTANCE, new SingleUniqueKey<>(NotAuditedType.ID).createIdentifier(id));
    }
}
