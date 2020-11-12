package com.kenshoo.pl.simulation.internal;

import com.kenshoo.pl.entity.EntityField;

public interface ActualResult {

    boolean isSuccess();

    default boolean isError() {
        return !isSuccess();
    }

    boolean isReallyChanged(EntityField<?, ?> field);

    Object getFinalValue(EntityField<?, ?> field);

    String getErrorDescription();

}
