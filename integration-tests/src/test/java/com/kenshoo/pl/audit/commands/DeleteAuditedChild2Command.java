package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.DeleteEntityCommand;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedChild2Type;

import static com.kenshoo.pl.entity.IdentifierType.uniqueKey;

public class DeleteAuditedChild2Command extends DeleteEntityCommand<AuditedChild2Type, Identifier<AuditedChild2Type>> {

    public DeleteAuditedChild2Command(final long id) {
        super(AuditedChild2Type.INSTANCE, uniqueKey(AuditedChild2Type.ID).createIdentifier(id));
    }
}
