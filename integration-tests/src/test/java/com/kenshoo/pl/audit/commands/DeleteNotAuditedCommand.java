package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedType;

import static com.kenshoo.pl.entity.IdentifierType.uniqueKey;

public class DeleteNotAuditedCommand extends DeleteEntityCommand<NotAuditedType, Identifier<NotAuditedType>>
    implements EntityCommandExt<NotAuditedType, DeleteNotAuditedCommand> {

    public DeleteNotAuditedCommand(final long id) {
        super(NotAuditedType.INSTANCE, uniqueKey(NotAuditedType.ID).createIdentifier(id));
    }
}
