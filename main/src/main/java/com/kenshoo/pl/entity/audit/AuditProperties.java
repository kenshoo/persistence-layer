package com.kenshoo.pl.entity.audit;

import com.kenshoo.pl.entity.TransientProperty;
import com.kenshoo.pl.entity.internal.TransientPropertyImpl;

public final class AuditProperties {

    public static final TransientProperty<String> ENTITY_CHANGE_DESCRIPTION = new TransientPropertyImpl<>("entity_change_description", String.class);

    private AuditProperties() {
        // constants class
    }
}
