package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.SingleUniqueKeyValue;
import com.kenshoo.pl.entity.UpdateEntityCommand;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedChildType;

public class UpdateNotAuditedChildCommand extends UpdateEntityCommand<NotAuditedChildType, Identifier<NotAuditedChildType>>
    implements EntityCommandExt<NotAuditedChildType, UpdateNotAuditedChildCommand> {

    public UpdateNotAuditedChildCommand(final long id) {
        super(NotAuditedChildType.INSTANCE, new SingleUniqueKeyValue<>(NotAuditedChildType.ID, id));
    }
}
