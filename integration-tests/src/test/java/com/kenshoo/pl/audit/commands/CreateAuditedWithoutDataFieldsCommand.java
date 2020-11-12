package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithoutDataFieldsType;

public class CreateAuditedWithoutDataFieldsCommand extends CreateEntityCommand<AuditedWithoutDataFieldsType>
    implements EntityCommandExt<AuditedWithoutDataFieldsType, CreateAuditedWithoutDataFieldsCommand> {

    public CreateAuditedWithoutDataFieldsCommand(final long id) {
        super(AuditedWithoutDataFieldsType.INSTANCE);
        set(AuditedWithoutDataFieldsType.ID, id);
    }
}
