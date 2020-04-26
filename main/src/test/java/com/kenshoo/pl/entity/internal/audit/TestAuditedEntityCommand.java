package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.*;

class TestAuditedEntityCommand extends ChangeEntityCommand<TestAuditedEntityType> implements EntityCommandExt<TestAuditedEntityType, TestAuditedEntityCommand> {

    private final ChangeOperation operator;
    private final Identifier<TestAuditedEntityType> identifier;

    TestAuditedEntityCommand(final long id, final ChangeOperation operator) {
        super(TestAuditedEntityType.INSTANCE);
        this.operator = operator;
        this.identifier = new SingleUniqueKeyValue<>(TestAuditedEntityType.ID, id);
    }

    @Override
    public Identifier<TestAuditedEntityType> getIdentifier() {
        return identifier;
    }

    @Override
    public ChangeOperation getChangeOperation() {
        return operator;
    }
}
