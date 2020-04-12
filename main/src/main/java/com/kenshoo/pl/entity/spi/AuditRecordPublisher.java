package com.kenshoo.pl.entity.spi;

import com.kenshoo.pl.entity.AuditRecord;

import java.util.Collection;

public interface AuditRecordPublisher {

    // Default implementation in case the client does not provide a publisher
    AuditRecordPublisher NO_OP = auditRecords -> {};

    void publish(final Collection<? extends AuditRecord<?>> auditRecords);
}
