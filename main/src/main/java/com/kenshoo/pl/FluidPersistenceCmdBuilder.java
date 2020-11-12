package com.kenshoo.pl;


import com.kenshoo.pl.entity.ChangeEntityCommand;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.internal.MissingChildrenSupplier;
import com.kenshoo.pl.entity.spi.FieldValueSupplier;

public class FluidPersistenceCmdBuilder<E extends EntityType<E>>{

    public ChangeEntityCommand<E> get() {
        return cmd;
    }

    private final ChangeEntityCommand<E> cmd;

    public FluidPersistenceCmdBuilder(ChangeEntityCommand<E> cmd) {
        this.cmd = cmd;
    }

    public static <E extends EntityType<E>> FluidPersistenceCmdBuilder<E> fluid(ChangeEntityCommand<E> cmd) {
        return new FluidPersistenceCmdBuilder<>(cmd);
    }

    public <T> FluidPersistenceCmdBuilder<E> with(EntityField<E, T> field, T value) {
        cmd.set(field, value);
        return this;
    }

    public <T> FluidPersistenceCmdBuilder<E> with(EntityField<E, T> field, FieldValueSupplier<T> valueSupplier) {
        cmd.set(field, valueSupplier);
        return this;
    }

    public <CHILD extends EntityType<CHILD>> FluidPersistenceCmdBuilder<E> withChild(ChangeEntityCommand<CHILD> childCmd) {
        cmd.addChild(childCmd);
        return this;
    }

    public <CHILD extends EntityType<CHILD>> FluidPersistenceCmdBuilder<E> withChild(FluidPersistenceCmdBuilder<CHILD> childCmd) {
        cmd.addChild(childCmd.get());
        return this;
    }

    public <CHILD extends EntityType<CHILD>> FluidPersistenceCmdBuilder<E> with(MissingChildrenSupplier<CHILD> s) {
        cmd.add(s);
        return this;
    }

}
