package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.DeleteEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.SingleUniqueKeyValue;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedType;

public class DeleteNotAuditedCommand extends DeleteEntityCommand<NotAuditedType, Identifier<NotAuditedType>>
    implements EntityCommandExt<NotAuditedType, DeleteNotAuditedCommand> {

    public DeleteNotAuditedCommand(final long id) {
        super(NotAuditedType.INSTANCE, new SingleUniqueKeyValue<>(NotAuditedType.ID, id));
    }
}
