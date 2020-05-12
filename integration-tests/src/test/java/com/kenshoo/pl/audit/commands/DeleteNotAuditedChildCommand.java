package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.DeleteEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.SingleUniqueKeyValue;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedChildType;

public class DeleteNotAuditedChildCommand extends DeleteEntityCommand<NotAuditedChildType, Identifier<NotAuditedChildType>>
    implements EntityCommandExt<NotAuditedChildType, DeleteNotAuditedChildCommand> {

    public DeleteNotAuditedChildCommand(final long id) {
        super(NotAuditedChildType.INSTANCE, new SingleUniqueKeyValue<>(NotAuditedChildType.ID, id));
    }
}
