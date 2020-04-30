package com.kenshoo.pl.audit;

import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.SingleUniqueKeyValue;
import com.kenshoo.pl.entity.UpdateEntityCommand;
import com.kenshoo.pl.entity.internal.audit.TestEntityType;

public class UpdateTestEntityCommand extends UpdateEntityCommand<TestEntityType, Identifier<TestEntityType>> implements EntityCommandExt<TestEntityType, UpdateTestEntityCommand> {

    public UpdateTestEntityCommand(final long id) {
        super(TestEntityType.INSTANCE, new SingleUniqueKeyValue<>(TestEntityType.ID, id));
    }
}
