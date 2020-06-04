package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.DeleteEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.SingleUniqueKeyValue;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedWithAncestorMandatoryType;

public class DeleteAuditedWithAncestorMandatoryCommand extends DeleteEntityCommand<AuditedWithAncestorMandatoryType, Identifier<AuditedWithAncestorMandatoryType>>
    implements EntityCommandExt<AuditedWithAncestorMandatoryType, DeleteAuditedWithAncestorMandatoryCommand> {

    public DeleteAuditedWithAncestorMandatoryCommand(final long id) {
        super(AuditedWithAncestorMandatoryType.INSTANCE, new SingleUniqueKeyValue<>(AuditedWithAncestorMandatoryType.ID, id));
    }
}
