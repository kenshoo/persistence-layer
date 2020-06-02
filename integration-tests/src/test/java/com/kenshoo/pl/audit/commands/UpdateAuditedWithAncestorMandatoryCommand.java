package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.SingleUniqueKeyValue;
import com.kenshoo.pl.entity.UpdateEntityCommand;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithAncestorMandatoryType;

public class UpdateAuditedWithAncestorMandatoryCommand extends UpdateEntityCommand<AuditedWithAncestorMandatoryType, Identifier<AuditedWithAncestorMandatoryType>>
    implements EntityCommandExt<AuditedWithAncestorMandatoryType, UpdateAuditedWithAncestorMandatoryCommand> {

    public UpdateAuditedWithAncestorMandatoryCommand(final long id) {
        super(AuditedWithAncestorMandatoryType.INSTANCE, new SingleUniqueKeyValue<>(AuditedWithAncestorMandatoryType.ID, id));
    }
}
