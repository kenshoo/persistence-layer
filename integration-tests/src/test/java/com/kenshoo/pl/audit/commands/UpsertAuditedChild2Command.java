package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedChild2Type;

public class UpsertAuditedChild2Command extends InsertOnDuplicateUpdateCommand<AuditedChild2Type, Identifier<AuditedChild2Type>>
    implements EntityCommandExt<AuditedChild2Type, UpsertAuditedChild2Command> {

    public UpsertAuditedChild2Command(final String name) {
        super(AuditedChild2Type.INSTANCE, new SingleUniqueKey<>(AuditedChild2Type.NAME).createIdentifier(name));
    }
}
