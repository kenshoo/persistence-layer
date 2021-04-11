package com.kenshoo.pl.entity.internal.audit;

import com.kenshoo.pl.entity.ChangeContext;
import com.kenshoo.pl.entity.ChangeFlowConfig;
import com.kenshoo.pl.entity.EntityChange;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.audit.AuditRecord;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class RecursiveAuditRecordGenerator {

    public <E extends EntityType<E>> Stream<? extends AuditRecord> generateMany(
        final ChangeFlowConfig<E> flowConfig,
        final Stream<? extends EntityChange<E>> entityChanges,
        final ChangeContext changeContext) {

        return flowConfig.auditRecordGenerator()
                         .map(auditRecordGenerator -> this.generateMany(flowConfig,
                                                                        auditRecordGenerator,
                                                                        entityChanges,
                                                                        changeContext))
                         .orElse(Stream.empty());
    }

    private <E extends EntityType<E>> Stream<? extends AuditRecord> generateMany(
        final ChangeFlowConfig<E> flowConfig,
        final AuditRecordGenerator<E> auditRecordGenerator,
        final Stream<? extends EntityChange<E>> entityChanges,
        final ChangeContext changeContext) {

        return entityChanges.map(entityChange -> this.generateOne(flowConfig,
                                                                  auditRecordGenerator,
                                                                  entityChange,
                                                                  changeContext))
                            .filter(Optional::isPresent)
                            .map(Optional::get);
    }

    private <E extends EntityType<E>> Optional<? extends AuditRecord> generateOne(
        final ChangeFlowConfig<E> flowConfig,
        final AuditRecordGenerator<E> auditRecordGenerator,
        final EntityChange<E> entityChange,
        final ChangeContext changeContext) {

        final Collection<? extends AuditRecord> childAuditRecords =
            flowConfig.childFlows().stream()
                      .flatMap(childFlowConfig -> generateChildrenUntyped(childFlowConfig,
                                                                          entityChange,
                                                                          changeContext))
                      .collect(toList());

        return auditRecordGenerator.generate(entityChange,
                                             changeContext,
                                             childAuditRecords);
    }

    private <E extends EntityType<E>> Stream<? extends AuditRecord> generateChildrenUntyped(
        final ChangeFlowConfig<? extends EntityType<?>> childFlowConfig,
        final EntityChange<E> entityChange,
        final ChangeContext changeContext) {

        return generateChildrenTyped(childFlowConfig,
                                     entityChange,
                                     changeContext);
    }

    private <P extends EntityType<P>, C extends EntityType<C>> Stream<? extends AuditRecord> generateChildrenTyped(
        final ChangeFlowConfig<C> childFlowConfig,
        final EntityChange<P> entityChange,
        final ChangeContext changeContext) {

        return generateMany(childFlowConfig,
                            entityChange.getChildren(childFlowConfig.getEntityType()),
                            changeContext);
    }
}