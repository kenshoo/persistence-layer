package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.SingleUniqueKeyValue;
import com.kenshoo.pl.entity.UpdateEntityCommand;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithoutDataFieldsType;

public class UpdateAuditedWithoutDataFieldsCommand extends UpdateEntityCommand<AuditedWithoutDataFieldsType, Identifier<AuditedWithoutDataFieldsType>>
    implements EntityCommandExt<AuditedWithoutDataFieldsType, UpdateAuditedWithoutDataFieldsCommand> {

    public UpdateAuditedWithoutDataFieldsCommand(final long id) {
        super(AuditedWithoutDataFieldsType.INSTANCE, new SingleUniqueKeyValue<>(AuditedWithoutDataFieldsType.ID, id));
    }
}
