package com.kenshoo.pl.audit.commands;

import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.InsertOnDuplicateUpdateCommand;
import com.kenshoo.pl.entity.SingleUniqueKeyValue;
import com.kenshoo.pl.entity.internal.audit.entitytypes.NotAuditedChildType;

public class UpsertNotAuditedChildCommand extends InsertOnDuplicateUpdateCommand<NotAuditedChildType, Identifier<NotAuditedChildType>>
    implements EntityCommandExt<NotAuditedChildType, UpsertNotAuditedChildCommand> {

    public UpsertNotAuditedChildCommand(final String name) {
        super(NotAuditedChildType.INSTANCE, new SingleUniqueKeyValue<>(NotAuditedChildType.NAME, name));
    }
}
