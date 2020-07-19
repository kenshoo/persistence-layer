package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.SingleUniqueKeyValue;
import com.kenshoo.pl.entity.UpdateEntityCommand;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithSelfMandatoryType;

public class UpdateAuditedWithSelfMandatoryCommand extends UpdateEntityCommand<AuditedWithSelfMandatoryType, Identifier<AuditedWithSelfMandatoryType>>
    implements EntityCommandExt<AuditedWithSelfMandatoryType, UpdateAuditedWithSelfMandatoryCommand> {

    public UpdateAuditedWithSelfMandatoryCommand(final long id) {
        super(AuditedWithSelfMandatoryType.INSTANCE, new SingleUniqueKeyValue<>(AuditedWithSelfMandatoryType.ID, id));
    }
}
