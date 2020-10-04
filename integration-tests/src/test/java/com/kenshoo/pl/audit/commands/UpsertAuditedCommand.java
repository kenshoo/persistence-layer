package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType;

public class UpsertAuditedCommand extends InsertOnDuplicateUpdateCommand<AuditedType, Identifier<AuditedType>> implements EntityCommandExt<AuditedType, UpsertAuditedCommand> {

    public UpsertAuditedCommand(final String name) {
        super(AuditedType.INSTANCE, new SingleUniqueKey<>(AuditedType.NAME).createIdentifier(name));
    }
}
