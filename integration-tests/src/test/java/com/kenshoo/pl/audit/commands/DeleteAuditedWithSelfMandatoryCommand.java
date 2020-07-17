package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.DeleteEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.SingleUniqueKeyValue;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithSelfMandatoryType;

public class DeleteAuditedWithSelfMandatoryCommand extends DeleteEntityCommand<AuditedWithSelfMandatoryType, Identifier<AuditedWithSelfMandatoryType>>
    implements EntityCommandExt<AuditedWithSelfMandatoryType, DeleteAuditedWithSelfMandatoryCommand> {

    public DeleteAuditedWithSelfMandatoryCommand(final long id) {
        super(AuditedWithSelfMandatoryType.INSTANCE, new SingleUniqueKeyValue<>(AuditedWithSelfMandatoryType.ID, id));
    }
}
