package com.kenshoo.pl.entity;

import java.util.Comparator;

public interface MutableEntityField<E extends EntityType<E>, T> extends EntityField<E, T> {

    EntityField<E, T> comparedBy(final Comparator<T> valueComparator);
}
