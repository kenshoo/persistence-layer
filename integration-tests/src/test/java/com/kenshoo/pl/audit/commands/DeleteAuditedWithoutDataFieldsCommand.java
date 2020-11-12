package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithoutDataFieldsType;

import static com.kenshoo.pl.entity.IdentifierType.uniqueKey;

public class DeleteAuditedWithoutDataFieldsCommand extends DeleteEntityCommand<AuditedWithoutDataFieldsType, Identifier<AuditedWithoutDataFieldsType>>
    implements EntityCommandExt<AuditedWithoutDataFieldsType, DeleteAuditedWithoutDataFieldsCommand> {

    public DeleteAuditedWithoutDataFieldsCommand(final long id) {
        super(AuditedWithoutDataFieldsType.INSTANCE, uniqueKey(AuditedWithoutDataFieldsType.ID).createIdentifier(id));
    }
}
