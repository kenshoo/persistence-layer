package com.kenshoo.pl.migration.internal;

import com.kenshoo.pl.entity.*;
import java.util.Collection;


public class SimulatedResult<E extends EntityType<E>, ID extends Identifier<E>> {
    final ChangeEntityCommand<E> cmd;
    final ID id;
    final Collection<ValidationError> errors;

    public SimulatedResult(ChangeEntityCommand<E> cmd, ID id, Collection<ValidationError> errors) {
        this.cmd = cmd;
        this.id = id;
        this.errors = errors;
    }

    public ID getId() {
        return id;
    }

    public boolean isError() {
        return errors.size() > 0;
    }

    public boolean isSuccess() {
        return errors.isEmpty();
    }

    public EntityChange<E> getCommand() {
        return cmd;
    }

    public Collection<ValidationError> getErrors() {
        return errors;
    }
}
