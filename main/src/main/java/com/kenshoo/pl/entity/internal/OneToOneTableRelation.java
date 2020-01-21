package com.kenshoo.pl.entity.internal;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jooq.Record;
import org.jooq.Table;

import static java.util.Objects.requireNonNull;

class OneToOneTableRelation {
    private final Table<Record> primary;
    private final Table<Record> secondary;

    private OneToOneTableRelation(final Table<Record> primary, final Table<Record> secondary) {
        this.primary = requireNonNull(primary, "A primary table must be specified");
        this.secondary = requireNonNull(secondary, "A secondary table must be specified");
    }

    Table<Record> getPrimary() {
        return primary;
    }

    Table<Record> getSecondary() {
        return secondary;
    }

    static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        OneToOneTableRelation that = (OneToOneTableRelation) o;

        return new EqualsBuilder()
            .append(primary, that.primary)
            .append(secondary, that.secondary)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(primary)
            .append(secondary)
            .toHashCode();
    }

    static class Builder {
        private Table<Record> primary;
        private Table<Record> secondary;

        Builder primary(Table<Record> primary) {
            this.primary = primary;
            return this;
        }

        Builder secondary(Table<Record> secondary) {
            this.secondary = secondary;
            return this;
        }

        OneToOneTableRelation build() {
            return new OneToOneTableRelation(primary, secondary);
        }
    }
}
