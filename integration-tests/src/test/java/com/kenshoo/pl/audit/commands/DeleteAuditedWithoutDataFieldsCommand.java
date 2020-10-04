package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithoutDataFieldsType;

public class DeleteAuditedWithoutDataFieldsCommand extends DeleteEntityCommand<AuditedWithoutDataFieldsType, Identifier<AuditedWithoutDataFieldsType>>
    implements EntityCommandExt<AuditedWithoutDataFieldsType, DeleteAuditedWithoutDataFieldsCommand> {

    public DeleteAuditedWithoutDataFieldsCommand(final long id) {
        super(AuditedWithoutDataFieldsType.INSTANCE, new SingleUniqueKey<>(AuditedWithoutDataFieldsType.ID).createIdentifier(id));
    }
}
