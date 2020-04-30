package com.kenshoo.pl.audit;

import com.kenshoo.pl.entity.AuditRecord;
import com.kenshoo.pl.entity.spi.AuditRecordPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

class InMemoryAuditRecordPublisher implements AuditRecordPublisher {

    private final List<AuditRecord<?>> auditRecords = new ArrayList<>();

    @Override
    public void publish(Stream<? extends AuditRecord<?>> auditRecords) {
        if (auditRecords != null) {
            this.auditRecords.addAll(auditRecords.collect(toList()));
        }
    }

    public Stream<? extends AuditRecord<?>> getAuditRecords() {
        return auditRecords.stream();
    }
}
