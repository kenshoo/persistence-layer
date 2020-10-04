package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedType;

public class UpsertNotAuditedCommand extends InsertOnDuplicateUpdateCommand<NotAuditedType, Identifier<NotAuditedType>> implements EntityCommandExt<NotAuditedType, UpsertNotAuditedCommand> {

    public UpsertNotAuditedCommand(final String name) {
        super(NotAuditedType.INSTANCE, new SingleUniqueKey<>(NotAuditedType.NAME).createIdentifier(name));
    }
}
