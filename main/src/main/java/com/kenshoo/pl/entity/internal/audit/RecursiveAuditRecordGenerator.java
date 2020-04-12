package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.*;

import java.util.Collection;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class RecursiveAuditRecordGenerator {

    public <E extends EntityType<E>> Collection<? extends AuditRecord<E>> generateMany(
        final ChangeFlowConfig<E> flowConfig,
        final Collection<? extends EntityChange<E>> entityChanges,
        final ChangeContext changeContext) {

        //noinspection RedundantTypeArguments
        return flowConfig.auditRecordGenerator()
                         .map(auditRecordGenerator -> this.<E>generateMany(flowConfig,
                                                                           auditRecordGenerator,
                                                                           entityChanges,
                                                                           changeContext))
                         .orElse(emptyList());
    }

    private <E extends EntityType<E>> Collection<? extends AuditRecord<E>> generateMany(
        final ChangeFlowConfig<E> flowConfig,
        final AuditRecordGenerator<E> auditRecordGenerator,
        final Collection<? extends EntityChange<E>> entityChanges,
        final ChangeContext changeContext) {

        return entityChanges.stream()
                            .map(entityChange -> generateOne(flowConfig,
                                                             auditRecordGenerator,
                                                             entityChange,
                                                             changeContext))
                            .collect(toList());
    }

    private <E extends EntityType<E>> AuditRecord<E> generateOne(final ChangeFlowConfig<E> flowConfig,
                                                                 final AuditRecordGenerator<E> auditRecordGenerator,
                                                                 final EntityChange<E> entityChange,
                                                                 final ChangeContext changeContext) {
        final Collection<? extends AuditRecord<?>> childAuditRecords =
            flowConfig.childFlows().stream()
                      .flatMap(childFlowConfig -> generateChildren(childFlowConfig,
                                                                   entityChange,
                                                                   changeContext).stream())
                      .collect(toList());

        return auditRecordGenerator.generate(entityChange,
                                             changeContext.getEntity(entityChange),
                                             childAuditRecords);
    }

    private <P extends EntityType<P>, C extends EntityType<C>> Collection<? extends AuditRecord<C>> generateChildren(
        final ChangeFlowConfig<C> childFlowConfig,
        final EntityChange<P> entityChange,
        final ChangeContext changeContext) {

        return generateMany(childFlowConfig,
                            entityChange.getChildren(childFlowConfig.getEntityType()).collect(toSet()),
                            changeContext);
    }
}