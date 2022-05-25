package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.DeleteEntityCommand;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedAutoIncIdChild1Type;

import static com.kenshoo.pl.entity.IdentifierType.uniqueKey;

public class DeleteAuditedChild1Command extends DeleteEntityCommand<AuditedAutoIncIdChild1Type, Identifier<AuditedAutoIncIdChild1Type>> {

    public DeleteAuditedChild1Command(final long id) {
        super(AuditedAutoIncIdChild1Type.INSTANCE, uniqueKey(AuditedAutoIncIdChild1Type.ID).createIdentifier(id));
    }
}
