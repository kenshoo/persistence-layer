package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedType;

import static com.kenshoo.pl.entity.IdentifierType.uniqueKey;

public class UpsertNotAuditedCommand extends InsertOnDuplicateUpdateCommand<NotAuditedType, Identifier<NotAuditedType>> implements EntityCommandExt<NotAuditedType, UpsertNotAuditedCommand> {

    public UpsertNotAuditedCommand(final String name) {
        super(NotAuditedType.INSTANCE, uniqueKey(NotAuditedType.NAME).createIdentifier(name));
    }
}
