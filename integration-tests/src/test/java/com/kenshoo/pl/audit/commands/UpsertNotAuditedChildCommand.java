package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedChildType;

public class UpsertNotAuditedChildCommand extends InsertOnDuplicateUpdateCommand<NotAuditedChildType, Identifier<NotAuditedChildType>>
    implements EntityCommandExt<NotAuditedChildType, UpsertNotAuditedChildCommand> {

    public UpsertNotAuditedChildCommand(final String name) {
        super(NotAuditedChildType.INSTANCE, new SingleUniqueKey<>(NotAuditedChildType.NAME).createIdentifier(name));
    }
}
