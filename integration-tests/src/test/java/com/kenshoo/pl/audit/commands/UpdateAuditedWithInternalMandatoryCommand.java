package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.SingleUniqueKeyValue;
import com.kenshoo.pl.entity.UpdateEntityCommand;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithInternalMandatoryType;

public class UpdateAuditedWithInternalMandatoryCommand extends UpdateEntityCommand<AuditedWithInternalMandatoryType, Identifier<AuditedWithInternalMandatoryType>>
    implements EntityCommandExt<AuditedWithInternalMandatoryType, UpdateAuditedWithInternalMandatoryCommand> {

    public UpdateAuditedWithInternalMandatoryCommand(final long id) {
        super(AuditedWithInternalMandatoryType.INSTANCE, new SingleUniqueKeyValue<>(AuditedWithInternalMandatoryType.ID, id));
    }
}
