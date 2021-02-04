package com.kenshoo.pl;


import com.kenshoo.pl.entity.ChangeEntityCommand;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.internal.MissingChildrenSupplier;
import com.kenshoo.pl.entity.spi.FieldValueSupplier;

public class TypedFluidPersistenceCmdBuilder<E extends EntityType<E>, CMD extends ChangeEntityCommand<E>> {

    public CMD get() {
        return cmd;
    }

    private final CMD cmd;

    public TypedFluidPersistenceCmdBuilder(CMD cmd) {
        this.cmd = cmd;
    }

    public static <E extends EntityType<E>, C extends ChangeEntityCommand<E>> TypedFluidPersistenceCmdBuilder<E, C> fluid(C cmd) {
        return new TypedFluidPersistenceCmdBuilder<>(cmd);
    }

    public <T> TypedFluidPersistenceCmdBuilder<E, CMD> with(EntityField<E, T> field, T value) {
        cmd.set(field, value);
        return this;
    }

    public <T> TypedFluidPersistenceCmdBuilder<E, CMD> with(EntityField<E, T> field, FieldValueSupplier<T> valueSupplier) {
        cmd.set(field, valueSupplier);
        return this;
    }

    public <CHILD extends EntityType<CHILD>, CHILDCMD extends ChangeEntityCommand<CHILD>> TypedFluidPersistenceCmdBuilder<E, CMD> withChild(CHILDCMD childCmd) {
        cmd.addChild(childCmd);
        return this;
    }

    public <CHILD extends EntityType<CHILD>, CHILDCMD extends ChangeEntityCommand<CHILD>> TypedFluidPersistenceCmdBuilder<E, CMD> withChild(TypedFluidPersistenceCmdBuilder<CHILD, CHILDCMD> childCmd) {
        cmd.addChild(childCmd.get());
        return this;
    }

    public <CHILD extends EntityType<CHILD>> TypedFluidPersistenceCmdBuilder<E, CMD> with(MissingChildrenSupplier<CHILD> s) {
        cmd.add(s);
        return this;
    }

}
