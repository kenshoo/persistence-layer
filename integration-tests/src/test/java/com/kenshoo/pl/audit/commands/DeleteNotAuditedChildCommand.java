package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.DeleteEntityCommand;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedChild1Type;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedChildType;

import static com.kenshoo.pl.entity.IdentifierType.uniqueKey;

public class DeleteNotAuditedChildCommand extends DeleteEntityCommand<NotAuditedChildType, Identifier<NotAuditedChildType>> {

    public DeleteNotAuditedChildCommand(final long id) {
        super(NotAuditedChildType.INSTANCE, uniqueKey(NotAuditedChildType.ID).createIdentifier(id));
    }
}
