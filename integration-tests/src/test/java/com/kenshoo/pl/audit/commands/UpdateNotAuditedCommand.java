package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.SingleUniqueKeyValue;
import com.kenshoo.pl.entity.UpdateEntityCommand;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedType;

public class UpdateNotAuditedCommand extends UpdateEntityCommand<NotAuditedType, Identifier<NotAuditedType>>
    implements EntityCommandExt<NotAuditedType, UpdateNotAuditedCommand> {

    public UpdateNotAuditedCommand(final long id) {
        super(NotAuditedType.INSTANCE, new SingleUniqueKeyValue<>(NotAuditedType.ID, id));
    }
}
