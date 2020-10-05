package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedChild1Type;

import static com.kenshoo.pl.entity.IdentifierType.uniqueKey;

public class UpdateAuditedChild1Command extends UpdateEntityCommand<AuditedChild1Type, Identifier<AuditedChild1Type>>
    implements EntityCommandExt<AuditedChild1Type, UpdateAuditedChild1Command> {

    public UpdateAuditedChild1Command(final long id) {
        super(AuditedChild1Type.INSTANCE, uniqueKey(AuditedChild1Type.ID).createIdentifier(id));
    }
}
