package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedAutoIncIdType;

class AuditedCommand extends ChangeEntityCommand<AuditedAutoIncIdType> implements EntityCommandExt<AuditedAutoIncIdType, AuditedCommand> {

    private final ChangeOperation operator;
    private final Identifier<AuditedAutoIncIdType> identifier;

    AuditedCommand(final long id, final ChangeOperation operator) {
        super(AuditedAutoIncIdType.INSTANCE);
        this.operator = operator;
        this.identifier = new SingleUniqueKey<>(AuditedAutoIncIdType.ID).createIdentifier(id);
    }

    @Override
    public Identifier<AuditedAutoIncIdType> getIdentifier() {
        return identifier;
    }

    @Override
    public ChangeOperation getChangeOperation() {
        return operator;
    }
}
