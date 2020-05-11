package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.DeleteEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.SingleUniqueKeyValue;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithoutDataFieldsType;

public class DeleteAuditedWithoutDataFieldsCommand extends DeleteEntityCommand<AuditedWithoutDataFieldsType, Identifier<AuditedWithoutDataFieldsType>>
    implements EntityCommandExt<AuditedWithoutDataFieldsType, DeleteAuditedWithoutDataFieldsCommand> {

    public DeleteAuditedWithoutDataFieldsCommand(final long id) {
        super(AuditedWithoutDataFieldsType.INSTANCE, new SingleUniqueKeyValue<>(AuditedWithoutDataFieldsType.ID, id));
    }
}
