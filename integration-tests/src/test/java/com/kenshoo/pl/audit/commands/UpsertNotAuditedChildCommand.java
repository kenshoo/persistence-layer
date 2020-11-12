package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedChildType;

import static com.kenshoo.pl.entity.IdentifierType.uniqueKey;

public class UpsertNotAuditedChildCommand extends InsertOnDuplicateUpdateCommand<NotAuditedChildType, Identifier<NotAuditedChildType>>
    implements EntityCommandExt<NotAuditedChildType, UpsertNotAuditedChildCommand> {

    public UpsertNotAuditedChildCommand(final String name) {
        super(NotAuditedChildType.INSTANCE, uniqueKey(NotAuditedChildType.NAME).createIdentifier(name));
    }
}
