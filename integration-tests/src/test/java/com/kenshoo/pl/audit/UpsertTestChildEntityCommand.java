package com.kenshoo.pl.audit;

import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.InsertOnDuplicateUpdateCommand;
import com.kenshoo.pl.entity.SingleUniqueKeyValue;
import com.kenshoo.pl.entity.internal.audit.TestChildEntityType;

public class UpsertTestChildEntityCommand extends InsertOnDuplicateUpdateCommand<TestChildEntityType, Identifier<TestChildEntityType>>
    implements EntityCommandExt<TestChildEntityType, UpsertTestChildEntityCommand> {

    public UpsertTestChildEntityCommand(final String name) {
        super(TestChildEntityType.INSTANCE, new SingleUniqueKeyValue<>(TestChildEntityType.NAME, name));
    }
}
