package com.kenshoo.pl.migration.internal;

import com.kenshoo.pl.entity.Entity;
import com.kenshoo.pl.entity.EntityField;
import java.util.Objects;


public class ActualSuccess implements ActualResult {

    final Entity stateBefore;
    final Entity stateAfter;

    public ActualSuccess(Entity stateBefore, Entity stateAfter) {
        this.stateBefore = stateBefore;
        this.stateAfter = stateAfter;
    }

    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public boolean isReallyChanged(EntityField<?, ?> field) {
        return Objects.equals(stateBefore.get(field), stateAfter.get(field));
    }

    @Override
    public Object getFinalValue(EntityField<?, ?> field) {
        return stateAfter.get(field);
    }

    @Override
    public String getErrorDescription() {
        return null;
    }
}
