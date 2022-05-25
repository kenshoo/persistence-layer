package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.InsertOnDuplicateUpdateCommand;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedAutoIncIdChild1Type;

import static com.kenshoo.pl.entity.IdentifierType.uniqueKey;

public class UpsertAuditedChild1Command extends InsertOnDuplicateUpdateCommand<AuditedAutoIncIdChild1Type, Identifier<AuditedAutoIncIdChild1Type>>
    implements EntityCommandExt<AuditedAutoIncIdChild1Type, UpsertAuditedChild1Command> {

    public UpsertAuditedChild1Command(final String name) {
        super(AuditedAutoIncIdChild1Type.INSTANCE, uniqueKey(AuditedAutoIncIdChild1Type.NAME).createIdentifier(name));
    }
}
