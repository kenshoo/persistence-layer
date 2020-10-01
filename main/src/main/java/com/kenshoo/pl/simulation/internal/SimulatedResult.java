package com.kenshoo.pl.simulation.internal;

import com.kenshoo.pl.entity.*;
import java.util.Collection;


public class SimulatedResult<E extends EntityType<E>> {
    final ChangeEntityCommand<E> cmd;
    final Identifier<E> id;
    final Collection<ValidationError> errors;

    public SimulatedResult(ChangeEntityCommand<E> cmd, Identifier<E> id, Collection<ValidationError> errors) {
        this.cmd = cmd;
        this.id = id;
        this.errors = errors;
    }

    public Identifier<E> getId() {
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
