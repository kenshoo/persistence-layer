package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.audit.AuditTrigger;

class AuditedFieldTrigger<E extends EntityType<E>> {
    private final AuditedField<E, ?> field;
    private final AuditTrigger trigger;

    AuditedFieldTrigger(final AuditedField<E, ?> field, final AuditTrigger trigger) {
        this.field = field;
        this.trigger = trigger;
    }

    AuditedField<E, ?> getField() {
        return field;
    }

    AuditTrigger getTrigger() {
        return trigger;
    }
}
