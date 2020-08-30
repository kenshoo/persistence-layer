package com.kenshoo.pl.migration.internal;

import com.kenshoo.pl.entity.EntityField;

public class ActualError implements ActualResult {
    final String description;

    public ActualError(String description) {
        this.description = description;
    }

    @Override public boolean isSuccess() {
        return false;
    }

    @Override public boolean isReallyChanged(EntityField<?, ?> field) {
        return false;

    }
    @Override public Object getFinalValue(EntityField<?, ?> field) {
        return null;
    }

    @Override
    public String getErrorDescription() {
        return description;
    }
}
