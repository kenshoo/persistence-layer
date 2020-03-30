package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.ChangeContext;
import com.kenshoo.pl.entity.ChangeFlowConfig;
import com.kenshoo.pl.entity.EntityChange;
import com.kenshoo.pl.entity.EntityType;

import java.util.Collection;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

public class EntityTreeChangeRecordGenerator {

    public static final EntityTreeChangeRecordGenerator INSTANCE = new EntityTreeChangeRecordGenerator();

    public <E extends EntityType<E>> Collection<? extends EntityTreeChangeRecord<E>> generateMany(
        final ChangeFlowConfig<E> flowConfig,
        final Collection<? extends EntityChange<E>> entityChanges,
        final ChangeContext changeContext) {

        //noinspection RedundantTypeArguments
        return flowConfig.optionalChangeRecordGenerator()
                         .map(changeRecordGenerator -> this.<E>generateMany(flowConfig,
                                                                            changeRecordGenerator,
                                                                            entityChanges,
                                                                            changeContext))
                         .orElse(emptySet());
    }

    private <E extends EntityType<E>> Collection<? extends EntityTreeChangeRecord<E>> generateMany(
        final ChangeFlowConfig<E> flowConfig,
        final EntityChangeRecordGenerator<E> changeRecordGenerator,
        final Collection<? extends EntityChange<E>> entityChanges,
        final ChangeContext changeContext) {

        return entityChanges.stream()
                            .map(entityChange -> generateOne(flowConfig,
                                                             changeRecordGenerator,
                                                             entityChange,
                                                             changeContext))
                            .collect(toSet());
    }

    private <E extends EntityType<E>> EntityTreeChangeRecord<E> generateOne(final ChangeFlowConfig<E> flowConfig,
                                                                            final EntityChangeRecordGenerator<E> changeRecordGenerator,
                                                                            final EntityChange<E> entityChange,
                                                                            final ChangeContext changeContext) {
        final EntityChangeRecord<E> changeRecord = changeRecordGenerator.generate(entityChange,
                                                                                  changeContext.getEntity(entityChange));
        final Collection<? extends EntityTreeChangeRecord<?>> childChangeRecords =
            flowConfig.childFlows().stream()
                      .flatMap(childFlowConfig -> generateChildren(childFlowConfig,
                                                                   entityChange,
                                                                   changeContext).stream())
                      .collect(toSet());

        return new EntityTreeChangeRecord<>(changeRecord, childChangeRecords);
    }

    private <P extends EntityType<P>, C extends EntityType<C>> Collection<? extends EntityTreeChangeRecord<C>> generateChildren(
        final ChangeFlowConfig<C> childFlowConfig,
        final EntityChange<P> entityChange,
        final ChangeContext changeContext) {

        return generateMany(childFlowConfig,
                            entityChange.getChildren(childFlowConfig.getEntityType()).collect(toSet()),
                            changeContext);
    }

    private EntityTreeChangeRecordGenerator() {
        // singleton
    }
}