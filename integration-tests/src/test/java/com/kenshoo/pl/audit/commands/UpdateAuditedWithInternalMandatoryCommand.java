package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithInternalMandatoryType;

public class UpdateAuditedWithInternalMandatoryCommand extends UpdateEntityCommand<AuditedWithInternalMandatoryType, Identifier<AuditedWithInternalMandatoryType>>
    implements EntityCommandExt<AuditedWithInternalMandatoryType, UpdateAuditedWithInternalMandatoryCommand> {

    public UpdateAuditedWithInternalMandatoryCommand(final long id) {
        super(AuditedWithInternalMandatoryType.INSTANCE, new SingleUniqueKey<>(AuditedWithInternalMandatoryType.ID).createIdentifier(id));
    }
}
