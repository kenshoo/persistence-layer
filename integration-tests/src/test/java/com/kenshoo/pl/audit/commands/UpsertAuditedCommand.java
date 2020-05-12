package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.InsertOnDuplicateUpdateCommand;
import com.kenshoo.pl.entity.SingleUniqueKeyValue;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType;

public class UpsertAuditedCommand extends InsertOnDuplicateUpdateCommand<AuditedType, Identifier<AuditedType>> implements EntityCommandExt<AuditedType, UpsertAuditedCommand> {

    public UpsertAuditedCommand(final String name) {
        super(AuditedType.INSTANCE, new SingleUniqueKeyValue<>(AuditedType.NAME, name));
    }
}
