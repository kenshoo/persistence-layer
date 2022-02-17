package com.kenshoo.pl.entity.audit;

import com.kenshoo.pl.entity.TransientProperty;
import com.kenshoo.pl.entity.internal.TransientPropertyImpl;

public final class AuditProperties {

    public static final TransientProperty<String> ENTITY_CHANGE_DESCRIPTION = new TransientPropertyImpl<>(
            "A description of the changes done to an entity that should be a part of an audit record - " +
                    "for example, the reason why the entity was changed.",
            String.class);

    private AuditProperties() {
        // constants class
    }
}
