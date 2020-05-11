package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.SingleUniqueKeyValue;
import com.kenshoo.pl.entity.UpdateEntityCommand;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType;

public class UpdateAuditedCommand extends UpdateEntityCommand<AuditedType, Identifier<AuditedType>> implements EntityCommandExt<AuditedType, UpdateAuditedCommand> {

    public UpdateAuditedCommand(final long id) {
        super(AuditedType.INSTANCE, new SingleUniqueKeyValue<>(AuditedType.ID, id));
    }
}
