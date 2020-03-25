package com.kenshoo.pl.changelog.entity;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Collection;

public class ChangeAction {
    private final String username;
    private final String actionId;
    private final Collection<? extends EntityChangeRecord<?>> entityChanges;

    public ChangeAction(final String username,
                        final String actionId,
                        final Collection<? extends EntityChangeRecord<?>> entityChanges) {
        this.username = username;
        this.actionId = actionId;
        this.entityChanges = entityChanges;
    }

    public String getUsername() {
        return username;
    }

    public String getActionId() {
        return actionId;
    }

    public Collection<? extends EntityChangeRecord<?>> getEntityChanges() {
        return entityChanges;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ChangeAction that = (ChangeAction) o;

        return new EqualsBuilder()
            .append(username, that.username)
            .append(actionId, that.actionId)
            .append(entityChanges, that.entityChanges)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(username)
            .append(actionId)
            .append(entityChanges)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("username", username)
            .append("actionId", actionId)
            .append("entityChanges", entityChanges)
            .toString();
    }
}
