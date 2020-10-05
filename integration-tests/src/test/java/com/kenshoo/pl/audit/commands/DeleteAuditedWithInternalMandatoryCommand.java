package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithInternalMandatoryType;

import static com.kenshoo.pl.entity.IdentifierType.uniqueKey;

public class DeleteAuditedWithInternalMandatoryCommand extends DeleteEntityCommand<AuditedWithInternalMandatoryType, Identifier<AuditedWithInternalMandatoryType>>
    implements EntityCommandExt<AuditedWithInternalMandatoryType, DeleteAuditedWithInternalMandatoryCommand> {

    public DeleteAuditedWithInternalMandatoryCommand(final long id) {
        super(AuditedWithInternalMandatoryType.INSTANCE, uniqueKey(AuditedWithInternalMandatoryType.ID).createIdentifier(id));
    }
}
