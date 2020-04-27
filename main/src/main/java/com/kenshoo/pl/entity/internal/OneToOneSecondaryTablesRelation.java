package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.EntityField;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toSet;
import static org.jooq.lambda.function.Functions.not;


public class OneToOneSecondaryTablesRelation {

    public Set<OneToOneTableRelation> find(Collection<? extends EntityField<?, ?>> fieldsToFetch) {
        return fieldsToFetch.stream()
                .filter(not(isOfPrimaryTable()))
                .map(field -> OneToOneTableRelation.builder()
                        .secondary(field.getDbAdapter().getTable())
                        .primary(field.getEntityType().getPrimaryTable())
                        .build())
                .collect(toSet());
    }

    private Predicate<EntityField<?, ?>> isOfPrimaryTable() {
        return field -> field.getDbAdapter().getTable().equals(field.getEntityType().getPrimaryTable());
    }
}
