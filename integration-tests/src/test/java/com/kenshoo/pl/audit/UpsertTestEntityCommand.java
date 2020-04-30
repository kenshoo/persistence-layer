package com.kenshoo.pl.audit;

import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.InsertOnDuplicateUpdateCommand;
import com.kenshoo.pl.entity.SingleUniqueKeyValue;
import com.kenshoo.pl.entity.internal.audit.TestEntityType;

public class UpsertTestEntityCommand extends InsertOnDuplicateUpdateCommand<TestEntityType, Identifier<TestEntityType>> implements EntityCommandExt<TestEntityType, UpsertTestEntityCommand> {

    public UpsertTestEntityCommand(final String name) {
        super(TestEntityType.INSTANCE, new SingleUniqueKeyValue<>(TestEntityType.NAME, name));
    }
}
