package com.kenshoo.pl.audit;

import com.kenshoo.pl.entity.DeleteEntityCommand;
import com.kenshoo.pl.entity.EntityCommandExt;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.SingleUniqueKeyValue;
import com.kenshoo.pl.entity.internal.audit.TestEntityType;

public class DeleteTestEntityCommand extends DeleteEntityCommand<TestEntityType, Identifier<TestEntityType>>
    implements EntityCommandExt<TestEntityType, DeleteTestEntityCommand> {

    public DeleteTestEntityCommand(final long id) {
        super(TestEntityType.INSTANCE, new SingleUniqueKeyValue<>(TestEntityType.ID, id));
    }
}
