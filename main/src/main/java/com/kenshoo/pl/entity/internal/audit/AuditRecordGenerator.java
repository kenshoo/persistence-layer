package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.ChangeContext;
import com.kenshoo.pl.entity.EntityChange;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.audit.AuditRecord;

import java.util.Collection;
import java.util.Optional;

public interface AuditRecordGenerator<E extends EntityType<E>> {
    Optional<AuditRecord<E>> generate(EntityChange<E> entityChange,
                                      ChangeContext context,
                                      Collection<? extends AuditRecord<?>> childRecords);
}
