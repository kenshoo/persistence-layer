package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.DeleteEntityCommand;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.SingleUniqueKeyValue;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedChild2Type;

public class DeleteAuditedChild2Command extends DeleteEntityCommand<AuditedChild2Type, Identifier<AuditedChild2Type>> {

    public DeleteAuditedChild2Command(final long id) {
        super(AuditedChild2Type.INSTANCE, new SingleUniqueKeyValue<>(AuditedChild2Type.ID, id));
    }
}