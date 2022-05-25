package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.UpdateEntityCommand;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedAutoIncIdChild1Type;

import static com.kenshoo.pl.entity.IdentifierType.uniqueKey;

public class UpdateAuditedChild1Command extends UpdateEntityCommand<AuditedAutoIncIdChild1Type, Identifier<AuditedAutoIncIdChild1Type>>
    implements EntityCommandExt<AuditedAutoIncIdChild1Type, UpdateAuditedChild1Command> {

    public UpdateAuditedChild1Command(final long id) {
        super(AuditedAutoIncIdChild1Type.INSTANCE, uniqueKey(AuditedAutoIncIdChild1Type.ID).createIdentifier(id));
    }
}
