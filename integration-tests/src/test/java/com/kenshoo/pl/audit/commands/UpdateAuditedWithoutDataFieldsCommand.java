package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithoutDataFieldsType;

public class UpdateAuditedWithoutDataFieldsCommand extends UpdateEntityCommand<AuditedWithoutDataFieldsType, Identifier<AuditedWithoutDataFieldsType>>
    implements EntityCommandExt<AuditedWithoutDataFieldsType, UpdateAuditedWithoutDataFieldsCommand> {

    public UpdateAuditedWithoutDataFieldsCommand(final long id) {
        super(AuditedWithoutDataFieldsType.INSTANCE, new SingleUniqueKey<>(AuditedWithoutDataFieldsType.ID).createIdentifier(id));
    }
}
