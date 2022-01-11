package com.kenshoo.pl.entity.spi;

import com.kenshoo.pl.entity.ChangeEntityCommand;
import com.kenshoo.pl.entity.EntityType;

public abstract class EnrichmentEvent {

    private ChangeEntityCommand<? extends EntityType<?>> source;

    protected EnrichmentEvent(ChangeEntityCommand<? extends EntityType<?>> source) {
        this.source = source;
    }

    public ChangeEntityCommand<? extends EntityType<?>> getSource() {
        return source;
    }

}
