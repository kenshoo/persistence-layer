package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.DeleteEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.SingleUniqueKeyValue;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType;

public class DeleteAuditedCommand extends DeleteEntityCommand<AuditedType, Identifier<AuditedType>>
    implements EntityCommandExt<AuditedType, DeleteAuditedCommand> {

    public DeleteAuditedCommand(final long id) {
        super(AuditedType.INSTANCE, new SingleUniqueKeyValue<>(AuditedType.ID, id));
    }
}
