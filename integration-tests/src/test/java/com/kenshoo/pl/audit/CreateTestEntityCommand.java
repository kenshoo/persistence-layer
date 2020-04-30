package com.kenshoo.pl.audit;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.internal.audit.TestEntityType;

public class CreateTestEntityCommand extends CreateEntityCommand<TestEntityType> implements EntityCommandExt<TestEntityType, CreateTestEntityCommand> {

    public CreateTestEntityCommand() {
        super(TestEntityType.INSTANCE);
    }
}
