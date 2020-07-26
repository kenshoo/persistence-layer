package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.DeleteEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.SingleUniqueKeyValue;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithInternalMandatoryType;

public class DeleteAuditedWithInternalMandatoryCommand extends DeleteEntityCommand<AuditedWithInternalMandatoryType, Identifier<AuditedWithInternalMandatoryType>>
    implements EntityCommandExt<AuditedWithInternalMandatoryType, DeleteAuditedWithInternalMandatoryCommand> {

    public DeleteAuditedWithInternalMandatoryCommand(final long id) {
        super(AuditedWithInternalMandatoryType.INSTANCE, new SingleUniqueKeyValue<>(AuditedWithInternalMandatoryType.ID, id));
    }
}
