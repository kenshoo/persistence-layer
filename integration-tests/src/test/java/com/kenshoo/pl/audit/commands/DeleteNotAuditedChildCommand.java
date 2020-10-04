package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedChildType;

public class DeleteNotAuditedChildCommand extends DeleteEntityCommand<NotAuditedChildType, Identifier<NotAuditedChildType>>
    implements EntityCommandExt<NotAuditedChildType, DeleteNotAuditedChildCommand> {

    public DeleteNotAuditedChildCommand(final long id) {
        super(NotAuditedChildType.INSTANCE, new SingleUniqueKey<>(NotAuditedChildType.ID).createIdentifier(id));
    }
}
