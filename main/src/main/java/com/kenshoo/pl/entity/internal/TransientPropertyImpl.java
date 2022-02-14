package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.TransientProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.Validate.notBlank;

public class TransientPropertyImpl<T> implements TransientProperty<T> {

    private final String name;
    private final Class<T> type;

    public TransientPropertyImpl(final String name, final Class<T> type) {
        this.name = notBlank(name, "A non-blank name is required");
        this.type = requireNonNull(type, "A type is required");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<T> getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        TransientPropertyImpl<?> that = (TransientPropertyImpl<?>) o;

        return new EqualsBuilder().append(name, that.name).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(name).toHashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}
