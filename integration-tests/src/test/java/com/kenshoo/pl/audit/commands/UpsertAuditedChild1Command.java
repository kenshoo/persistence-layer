package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.InsertOnDuplicateUpdateCommand;
import com.kenshoo.pl.entity.SingleUniqueKeyValue;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedChild1Type;

public class UpsertAuditedChild1Command extends InsertOnDuplicateUpdateCommand<AuditedChild1Type, Identifier<AuditedChild1Type>>
    implements EntityCommandExt<AuditedChild1Type, UpsertAuditedChild1Command> {

    public UpsertAuditedChild1Command(final String name) {
        super(AuditedChild1Type.INSTANCE, new SingleUniqueKeyValue<>(AuditedChild1Type.NAME, name));
    }
}
