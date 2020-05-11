package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.SingleUniqueKeyValue;
import com.kenshoo.pl.entity.UpdateEntityCommand;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedChild1Type;

public class UpdateAuditedChild1Command extends UpdateEntityCommand<AuditedChild1Type, Identifier<AuditedChild1Type>>
    implements EntityCommandExt<AuditedChild1Type, UpdateAuditedChild1Command> {

    public UpdateAuditedChild1Command(final long id) {
        super(AuditedChild1Type.INSTANCE, new SingleUniqueKeyValue<>(AuditedChild1Type.ID, id));
    }
}
