package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedChild2Type;

import static com.kenshoo.pl.entity.IdentifierType.uniqueKey;

public class UpdateAuditedChild2Command extends UpdateEntityCommand<AuditedChild2Type, Identifier<AuditedChild2Type>>
    implements EntityCommandExt<AuditedChild2Type, UpdateAuditedChild2Command> {

    public UpdateAuditedChild2Command(final long id) {
        super(AuditedChild2Type.INSTANCE, uniqueKey(AuditedChild2Type.ID).createIdentifier(id));
    }
}
