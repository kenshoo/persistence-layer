package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithoutDataFieldsType;

import static com.kenshoo.pl.entity.IdentifierType.uniqueKey;

public class UpdateAuditedWithoutDataFieldsCommand extends UpdateEntityCommand<AuditedWithoutDataFieldsType, Identifier<AuditedWithoutDataFieldsType>>
    implements EntityCommandExt<AuditedWithoutDataFieldsType, UpdateAuditedWithoutDataFieldsCommand> {

    public UpdateAuditedWithoutDataFieldsCommand(final long id) {
        super(AuditedWithoutDataFieldsType.INSTANCE, uniqueKey(AuditedWithoutDataFieldsType.ID).createIdentifier(id));
    }
}
