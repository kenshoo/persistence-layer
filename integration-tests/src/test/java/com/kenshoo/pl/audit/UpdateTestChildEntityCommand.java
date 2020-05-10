package com.kenshoo.pl.audit;

import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.SingleUniqueKeyValue;
import com.kenshoo.pl.entity.UpdateEntityCommand;
import com.kenshoo.pl.entity.internal.audit.TestChildEntityType;

public class UpdateTestChildEntityCommand extends UpdateEntityCommand<TestChildEntityType, Identifier<TestChildEntityType>>
    implements EntityCommandExt<TestChildEntityType, UpdateTestChildEntityCommand> {

    public UpdateTestChildEntityCommand(final long id) {
        super(TestChildEntityType.INSTANCE, new SingleUniqueKeyValue<>(TestChildEntityType.ID, id));
    }
}
