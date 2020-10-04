package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedChild2Type;

public class UpdateAuditedChild2Command extends UpdateEntityCommand<AuditedChild2Type, Identifier<AuditedChild2Type>>
    implements EntityCommandExt<AuditedChild2Type, UpdateAuditedChild2Command> {

    public UpdateAuditedChild2Command(final long id) {
        super(AuditedChild2Type.INSTANCE, new SingleUniqueKey<>(AuditedChild2Type.ID).createIdentifier(id));
    }
}
