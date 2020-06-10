package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.audit.AuditTrigger;

class AuditedFieldTrigger<E extends EntityType<E>> {
    private final EntityField<E, ?> field;
    private final AuditTrigger trigger;

    AuditedFieldTrigger(final EntityField<E, ?> field, final AuditTrigger trigger) {
        this.field = field;
        this.trigger = trigger;
    }

    EntityField<E, ?> getField() {
        return field;
    }

    AuditTrigger getTrigger() {
        return trigger;
    }
}
