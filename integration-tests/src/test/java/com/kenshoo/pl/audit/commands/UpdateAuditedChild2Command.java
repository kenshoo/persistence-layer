package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.UpdateEntityCommand;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedAutoIncIdChild2Type;

import static com.kenshoo.pl.entity.IdentifierType.uniqueKey;

public class UpdateAuditedChild2Command extends UpdateEntityCommand<AuditedAutoIncIdChild2Type, Identifier<AuditedAutoIncIdChild2Type>>
    implements EntityCommandExt<AuditedAutoIncIdChild2Type, UpdateAuditedChild2Command> {

    public UpdateAuditedChild2Command(final long id) {
        super(AuditedAutoIncIdChild2Type.INSTANCE, uniqueKey(AuditedAutoIncIdChild2Type.ID).createIdentifier(id));
    }
}
