package com.kenshoo.pl.audit;

import com.kenshoo.pl.entity.DeleteEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.SingleUniqueKeyValue;
import com.kenshoo.pl.entity.internal.audit.TestChildEntityType;

public class DeleteTestChildEntityCommand extends DeleteEntityCommand<TestChildEntityType, Identifier<TestChildEntityType>>
    implements EntityCommandExt<TestChildEntityType, DeleteTestChildEntityCommand> {

    public DeleteTestChildEntityCommand(final long id) {
        super(TestChildEntityType.INSTANCE, new SingleUniqueKeyValue<>(TestChildEntityType.ID, id));
    }
}
