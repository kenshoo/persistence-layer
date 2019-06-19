package com.kenshoo.pl.entity;

import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.Collections;

public abstract class EntityChangeResult<E extends EntityType<E>, ID extends Identifier<E>, C extends ChangeEntityCommand<E>> {

    private final C command;
    private final Collection<ValidationError> errors;

    public EntityChangeResult(C command) {
        this(command, Collections.emptyList());
    }

    public EntityChangeResult(C command, Iterable<ValidationError> errors) {
        this.command = command;
        this.errors = ImmutableList.copyOf(errors);
    }

    public C getCommand() {
        return command;
    }

    public boolean isSuccess() {
        return errors.isEmpty();
    }

    public Collection<ValidationError> getErrors() {
        return errors;
    }

    public abstract ID getIdentifier();
}
