package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.InsertOnDuplicateUpdateCommand;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedAutoIncIdChild2Type;

import static com.kenshoo.pl.entity.IdentifierType.uniqueKey;

public class UpsertAuditedChild2Command extends InsertOnDuplicateUpdateCommand<AuditedAutoIncIdChild2Type, Identifier<AuditedAutoIncIdChild2Type>>
    implements EntityCommandExt<AuditedAutoIncIdChild2Type, UpsertAuditedChild2Command> {

    public UpsertAuditedChild2Command(final String name) {
        super(AuditedAutoIncIdChild2Type.INSTANCE, uniqueKey(AuditedAutoIncIdChild2Type.NAME).createIdentifier(name));
    }
}
