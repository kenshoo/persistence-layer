package com.kenshoo.pl.entity.internal;

import com.google.common.collect.ImmutableList;
import com.kenshoo.pl.entity.UniqueKey;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.fetch.*;
import org.jooq.*;

import java.util.*;

import static com.kenshoo.pl.entity.Feature.FetchMany;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.Validate.notEmpty;
import static org.jooq.lambda.Seq.seq;


public class EntitiesFetcher {

    private final DSLContext dslContext;
    private final FeatureSet features;
    private final OldEntityFetcher oldEntityFetcher;
    private final EntityFetcher entityFetcher;

    public EntitiesFetcher(DSLContext dslContext) {
        this(dslContext, FeatureSet.EMPTY);
    }

    public EntitiesFetcher(DSLContext dslContext, FeatureSet features) {
        this.dslContext = dslContext;
        this.features = features;
        this.oldEntityFetcher = new OldEntityFetcher(dslContext);
        this.entityFetcher = new EntityFetcher(dslContext);
    }

    public <E extends EntityType<E>> Map<Identifier<E>, Entity> fetchEntitiesByKeys(final E entityType,
                                                                                    final UniqueKey<E> uniqueKey,
                                                                                    final Collection<? extends Identifier<E>> keys,
                                                                                    final Collection<? extends EntityField<?, ?>> fieldsToFetch) {
        return oldEntityFetcher.fetchEntitiesByIds(keys, fieldsToFetch);

    }

    public <E extends EntityType<E>> Map<Identifier<E>, Entity> fetchEntitiesByIds(final Collection<? extends Identifier<E>> ids,
                                                                                   final EntityField<?, ?>... fieldsToFetchArgs) {
        return fetchEntitiesByIds(ids, ImmutableList.copyOf(fieldsToFetchArgs));
    }

    public <E extends EntityType<E>> Map<Identifier<E>, Entity> fetchEntitiesByIds(final Collection<? extends Identifier<E>> ids,
                                                                                   final Collection<? extends EntityField<?, ?>> fieldsToFetch) {
        return features.isEnabled(FetchMany) ? entityFetcher.fetchEntitiesByIds(ids, fieldsToFetch) : new OldEntityFetcher(dslContext).fetchEntitiesByIds(ids, fieldsToFetch);
    }

    public List<Entity> fetch(final EntityType<?> entityType,
                              final PLCondition plCondition,
                              final EntityField<?, ?>... fieldsToFetch) {
        return oldEntityFetcher.fetch(entityType, plCondition, fieldsToFetch);
    }

    public <E extends EntityType<E>> Map<Identifier<E>, Entity> fetchEntitiesByForeignKeys(E entityType, UniqueKey<E> foreignUniqueKey, Collection<? extends Identifier<E>> keys, Collection<EntityField<?, ?>> fieldsToFetch) {
        return oldEntityFetcher.fetchEntitiesByForeignKeys(entityType, foreignUniqueKey, keys, fieldsToFetch);
    }

    public <E extends EntityType<E>, PE extends PartialEntity, ID extends Identifier<E>> Map<ID, PE> fetchPartialEntities(E entityType, Collection<ID> keys, final Class<PE> entityIface) {
        return oldEntityFetcher.fetchPartialEntities(entityType, keys, entityIface);
    }

    public <E extends EntityType<E>, PE extends PartialEntity> List<PE> fetchByCondition(E entityType, Condition condition, final Class<PE> entityIface) {
        return oldEntityFetcher.fetchByCondition(entityType, condition, entityIface);
    }
}
