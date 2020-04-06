package com.kenshoo.pl.entity.internal.changelog;

import com.kenshoo.pl.entity.EntityType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Collection;

import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;

public class EntityTreeChangeRecord<E extends EntityType<E>> {
    private final EntityChangeRecord<E> changeRecord;
    private final Collection<? extends EntityTreeChangeRecord<?>> childChangeRecords;

    public EntityTreeChangeRecord(final EntityChangeRecord<E> changeRecord) {
        this(changeRecord, emptySet());
    }

    public EntityTreeChangeRecord(final EntityChangeRecord<E> changeRecord,
                                  final Collection<? extends EntityTreeChangeRecord<?>> childChangeRecords) {
        this.changeRecord = requireNonNull(changeRecord, "There must be a change record");
        this.childChangeRecords = childChangeRecords == null ? emptySet() : childChangeRecords;
    }

    public EntityChangeRecord<E> getChangeRecord() {
        return changeRecord;
    }

    public Collection<? extends EntityTreeChangeRecord<?>> getChildChangeRecords() {
        return childChangeRecords;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        EntityTreeChangeRecord<?> that = (EntityTreeChangeRecord<?>) o;

        return new EqualsBuilder()
            .append(changeRecord, that.changeRecord)
            .append(childChangeRecords, that.childChangeRecords)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(changeRecord)
            .append(childChangeRecords)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("changeRecord", changeRecord)
            .append("childChangeRecords", childChangeRecords)
            .toString();
    }
}
