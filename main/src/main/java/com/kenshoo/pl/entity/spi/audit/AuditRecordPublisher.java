package com.kenshoo.pl.entity.spi.audit;

import com.kenshoo.pl.entity.audit.AuditRecord;

import java.util.stream.Stream;

public interface AuditRecordPublisher {

    // Default implementation in case the client does not provide a publisher
    AuditRecordPublisher NO_OP = auditRecords -> {};

    void publish(final Stream<? extends AuditRecord> auditRecords);
}
