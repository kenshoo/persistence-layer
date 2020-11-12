package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.audit.entitytypes.AuditedType;

class AuditedCommand extends ChangeEntityCommand<AuditedType> implements EntityCommandExt<AuditedType, AuditedCommand> {

    private final ChangeOperation operator;
    private final Identifier<AuditedType> identifier;

    AuditedCommand(final long id, final ChangeOperation operator) {
        super(AuditedType.INSTANCE);
        this.operator = operator;
        this.identifier = new SingleUniqueKey<>(AuditedType.ID).createIdentifier(id);
    }

    @Override
    public Identifier<AuditedType> getIdentifier() {
        return identifier;
    }

    @Override
    public ChangeOperation getChangeOperation() {
        return operator;
    }
}
