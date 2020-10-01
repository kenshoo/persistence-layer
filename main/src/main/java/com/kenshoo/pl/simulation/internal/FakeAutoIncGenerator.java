package com.kenshoo.pl.simulation.internal;

import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.EntityWithGeneratedId;
import com.kenshoo.pl.entity.spi.OutputGenerator;
import java.util.Collection;


public class FakeAutoIncGenerator<E extends EntityType<E>> implements OutputGenerator<E> {

    private final E entityType;
    private final Object SOME_FAKE_VALUE = new Object();

    public FakeAutoIncGenerator(E entityType) {
        this.entityType = entityType;
    }

    @Override
    public void generate(Collection<? extends EntityChange<E>> commands, ChangeOperation op, ChangeContext ctx) {
        entityType.getPrimaryIdentityField().ifPresent(autoIncId -> populateFakeValue(autoIncId, commands, ctx));
    }

    private void populateFakeValue(EntityField<E, Object> field, Collection<? extends EntityChange<E>> commands, ChangeContext ctx) {
        commands.forEach(cmd -> ctx.addEntity(cmd, new EntityWithGeneratedId(field, SOME_FAKE_VALUE)));
    }

}
