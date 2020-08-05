package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.spi.CurrentStateConsumer;

public interface AuditRecordGeneratorStateConsumer<E extends EntityType<E>> extends AuditRecordGenerator<E>, CurrentStateConsumer<E> {
}
