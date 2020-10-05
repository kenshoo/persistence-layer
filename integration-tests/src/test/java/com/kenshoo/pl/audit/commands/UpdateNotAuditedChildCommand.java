package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedChildType;

import static com.kenshoo.pl.entity.IdentifierType.uniqueKey;

public class UpdateNotAuditedChildCommand extends UpdateEntityCommand<NotAuditedChildType, Identifier<NotAuditedChildType>>
    implements EntityCommandExt<NotAuditedChildType, UpdateNotAuditedChildCommand> {

    public UpdateNotAuditedChildCommand(final long id) {
        super(NotAuditedChildType.INSTANCE, uniqueKey(NotAuditedChildType.ID).createIdentifier(id));
    }
}
