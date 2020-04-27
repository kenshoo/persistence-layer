package com.kenshoo.pl.entity.internal;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.jooq.QueryExtension;
import com.kenshoo.pl.entity.UniqueKey;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.internal.fetch.QueryBuilder;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.lambda.Seq;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;


public class ChildrenIdFetcher<PARENT extends EntityType<PARENT>, CHILD extends EntityType<CHILD>> {

    private final DSLContext jooq;

    public ChildrenIdFetcher(DSLContext jooq) {
        this.jooq = jooq;
    }

    public Stream<FullIdentifier<PARENT, CHILD>>
    fetch(Collection<? extends Identifier<PARENT>> parentIds, UniqueKey<CHILD> childKey) {

        if (parentIds.isEmpty()) {
            return Stream.empty();
        }

        final PARENT parentType = first(parentIds).getUniqueKey().getEntityType();
        final CHILD childType = childKey.getEntityType();
        final EntityType.ForeignKey<CHILD, PARENT> keyToParent = childType.getKeyTo(parentType);
        final UniqueKey<PARENT> parentKey = first(parentIds).getUniqueKey();
        final UniqueKey<CHILD> childFK = new UniqueKey<>(keyToParent.from());
        final DataTable childTable = childType.getPrimaryTable();

        final SelectFinalStep<Record> query = jooq.select(getTableFields(Seq.concat(
                Seq.of(parentKey.getFields()),
                Seq.of(childKey.getFields()),
                Seq.of(childFK.getFields())))
        )
                .from(childTable)
                .join(parentType.getPrimaryTable())
                .on(everyFieldOf(keyToParent));

        final QueryExtension<SelectFinalStep<Record>> queryExtender = new QueryBuilder(jooq).addIdsCondition(query, childTable, parentKey, parentIds);
        final ResultQuery<Record> finalQuery = queryExtender.getQuery();
        return finalQuery.stream()
                .map(record -> readIdentifiers(record, parentKey, childKey, childFK))
                .onClose(() -> {
                    queryExtender.close();
                    query.close();
                });
    }

    private Condition everyFieldOf(EntityType.ForeignKey<CHILD,PARENT> keyToParent) {
        // TODO: Test keyToParent with more than one field
        List<Condition> conditions = keyToParent.references.stream().map(ref -> dbFieldOf(ref.v1).eq(dbFieldOf(ref.v2))).collect(toList());
        return DSL.and(conditions);
    }

    private Field dbFieldOf(EntityField<?,?> field) {
        return field.getDbAdapter().getTableFields().findFirst().get();
    }

    private FullIdentifier<PARENT, CHILD>
    readIdentifiers(Record record, UniqueKey<PARENT> parentKey, UniqueKey<CHILD> childKey, UniqueKey<CHILD> childFK) {
        return new FullIdentifier<>(
                parse(parentKey, record),
                parse(childKey, record),
                parse(childFK, record)
        );
    }

    private <T extends EntityType<T>> Identifier<T> parse(UniqueKey<T> key, Record values) {
        final Object[] ids = Seq.of(key.getFields())
                .map(field -> {
                    Stream dbValues = field.getDbAdapter().getTableFields().map(values::get);
                    return field.getDbAdapter().getFromRecord(dbValues.iterator());
                })
                .toArray();
        return new UniqueKeyValue<>(key, ids);
    }

    private Collection<TableField<Record, ?>>
    getTableFields(Stream<EntityField<?, ?>> entityFields) {
        return entityFields.flatMap(f -> f.getDbAdapter().getTableFields()).collect(toList());
    }

    private <T> T first(Collection<T> collection) {
        return collection.iterator().next();
    }
}
