package com.kenshoo.pl.audit;

import com.kenshoo.pl.entity.CreateEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.internal.audit.TestChildEntityType;

public class CreateTestChildEntityCommand extends CreateEntityCommand<TestChildEntityType> implements EntityCommandExt<TestChildEntityType, CreateTestChildEntityCommand> {

    public CreateTestChildEntityCommand() {
        super(TestChildEntityType.INSTANCE);
    }
}
