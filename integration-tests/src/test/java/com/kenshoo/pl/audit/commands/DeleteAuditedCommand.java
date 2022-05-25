package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.DeleteEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedAutoIncIdType;

import static com.kenshoo.pl.entity.IdentifierType.uniqueKey;

public class DeleteAuditedCommand extends DeleteEntityCommand<AuditedAutoIncIdType, Identifier<AuditedAutoIncIdType>>
    implements EntityCommandExt<AuditedAutoIncIdType, DeleteAuditedCommand> {

    public DeleteAuditedCommand(final long id) {
        super(AuditedAutoIncIdType.INSTANCE, uniqueKey(AuditedAutoIncIdType.ID).createIdentifier(id));
    }
}
