package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.DeleteEntityCommand;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedChild1Type;

import static com.kenshoo.pl.entity.IdentifierType.uniqueKey;

public class DeleteAuditedChild1Command extends DeleteEntityCommand<AuditedChild1Type, Identifier<AuditedChild1Type>> {

    public DeleteAuditedChild1Command(final long id) {
        super(AuditedChild1Type.INSTANCE, uniqueKey(AuditedChild1Type.ID).createIdentifier(id));
    }
}
