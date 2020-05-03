package com.kenshoo.pl.entity.internal.fetch;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.kenshoo.jooq.*;
import com.kenshoo.pl.data.ImpersonatorTable;
import com.kenshoo.pl.entity.UniqueKey;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.EntityImpl;
import com.kenshoo.pl.entity.internal.EntityTypeReflectionUtil;
import com.kenshoo.pl.entity.internal.PartialEntityInvocationHandler;
import org.jooq.*;
import org.jooq.lambda.Seq;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.function.Predicate;

import static com.kenshoo.pl.entity.Feature.FetchMany;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;
import static org.apache.commons.lang3.Validate.notEmpty;
import static org.jooq.lambda.Seq.seq;
import static org.jooq.lambda.function.Functions.not;


public interface Fetcher {

    /**
     * @deprecated replaced by {@link #fetchEntitiesByIds(Collection, EntityField[])} or {@link #fetchEntitiesByIds(Collection, Collection)}
     */
    @Deprecated
    default <E extends EntityType<E>> Map<Identifier<E>, Entity> fetchEntitiesByKeys(E entityType, UniqueKey<E> uniqueKey, Collection<? extends Identifier<E>> keys, Collection<? extends EntityField<?, ?>> fieldsToFetch) {
        return fetchEntitiesByIds(keys, fieldsToFetch);
    }

    default <E extends EntityType<E>> Map<Identifier<E>, Entity> fetchEntitiesByIds(Collection<? extends Identifier<E>> ids, EntityField<?, ?>... fieldsToFetchArgs){
        return fetchEntitiesByIds(ids, ImmutableList.copyOf(fieldsToFetchArgs));
    }

    <E extends EntityType<E>> Map<Identifier<E>, Entity> fetchEntitiesByIds(Collection<? extends Identifier<E>> ids, Collection<? extends EntityField<?, ?>> fieldsToFetch);

    List<Entity> fetch(EntityType<?> entityType, PLCondition plCondition, EntityField<?, ?>... fieldsToFetch);

    <E extends EntityType<E>> Map<Identifier<E>, Entity> fetchEntitiesByForeignKeys(E entityType, UniqueKey<E> foreignUniqueKey, Collection<? extends Identifier<E>> keys, Collection<EntityField<?, ?>> fieldsToFetch);

    <E extends EntityType<E>, PE extends PartialEntity, ID extends Identifier<E>> Map<ID, PE> fetchPartialEntities(E entityType, Collection<ID> keys, final Class<PE> entityIface);

    <E extends EntityType<E>, PE extends PartialEntity> List<PE> fetchByCondition(E entityType, Condition condition, Class<PE> entityIface);
}