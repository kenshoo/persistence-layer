package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.InsertOnDuplicateUpdateCommand;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedAutoIncIdType;

import static com.kenshoo.pl.entity.IdentifierType.uniqueKey;

public class UpsertAuditedCommand extends InsertOnDuplicateUpdateCommand<AuditedAutoIncIdType, Identifier<AuditedAutoIncIdType>> implements EntityCommandExt<AuditedAutoIncIdType, UpsertAuditedCommand> {

    public UpsertAuditedCommand(final String name) {
        super(AuditedAutoIncIdType.INSTANCE, uniqueKey(AuditedAutoIncIdType.NAME).createIdentifier(name));
    }
}
