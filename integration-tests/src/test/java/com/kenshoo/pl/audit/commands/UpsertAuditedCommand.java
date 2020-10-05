package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType;

import static com.kenshoo.pl.entity.IdentifierType.uniqueKey;

public class UpsertAuditedCommand extends InsertOnDuplicateUpdateCommand<AuditedType, Identifier<AuditedType>> implements EntityCommandExt<AuditedType, UpsertAuditedCommand> {

    public UpsertAuditedCommand(final String name) {
        super(AuditedType.INSTANCE, uniqueKey(AuditedType.NAME).createIdentifier(name));
    }
}
