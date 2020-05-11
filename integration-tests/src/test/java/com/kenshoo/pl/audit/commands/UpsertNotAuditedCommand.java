package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.InsertOnDuplicateUpdateCommand;
import com.kenshoo.pl.entity.SingleUniqueKeyValue;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedType;

public class UpsertNotAuditedCommand extends InsertOnDuplicateUpdateCommand<NotAuditedType, Identifier<NotAuditedType>> implements EntityCommandExt<NotAuditedType, UpsertNotAuditedCommand> {

    public UpsertNotAuditedCommand(final String name) {
        super(NotAuditedType.INSTANCE, new SingleUniqueKeyValue<>(NotAuditedType.NAME, name));
    }
}
