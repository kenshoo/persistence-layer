package com.kenshoo.pl.entity.internal;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.jooq.QueryExtension;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.UniqueKey;
import org.jooq.*;
import org.jooq.lambda.Seq;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Stream.empty;
import static org.jooq.lambda.Seq.seq;

public class ChildrenIdFetcher<PARENT extends EntityType<PARENT>, CHILD extends EntityType<CHILD>> {

    private final DSLContext jooq;

    public ChildrenIdFetcher(DSLContext jooq) {
        this.jooq = jooq;
    }

    public Stream<FullIdentifier<CHILD>>
    fetch(Collection<? extends ChangeEntityCommand<PARENT>> parents, CHILD childType) {
        if (parents.isEmpty()) {
            return empty();
        }
        UniqueKey<CHILD> partialChildKey = getPartialChildKey(parents, childType);
        final Set<Identifier<CHILD>> parentIds = collectParentIds(parents, childType);
        return queryForChildrenIds(partialChildKey, parentIds);
    }

    private Set<Identifier<CHILD>>
    collectParentIds(Collection<? extends ChangeEntityCommand<PARENT>> parents, CHILD childType) {
        final EntityType.ForeignKey<CHILD, PARENT> keyToParent = childType.getKeyTo(first(parents).getEntityType());
        return seq(parents)
                .map(parentCmd -> {
                    Object[] values = keyToParent.to().stream().map(field -> parentCmd.getIdentifier().get(field)).toArray();
                    return (Identifier<CHILD>) new UniqueKeyValue<>(new UniqueKey<>(keyToParent.from()), values);
                }).toSet();
    }

    private Stream<FullIdentifier<CHILD>>
    queryForChildrenIds(UniqueKey<CHILD> childKey, Set<Identifier<CHILD>> parentIds) {
        final UniqueKey<CHILD> parentKey = first(parentIds).getUniqueKey();
        final List<EntityField<CHILD, ?>> fullFields = Seq.of(parentKey.getFields()).concat(childKey.getFields()).toList();
        final DataTable primaryTable = childKey.getEntityType().getPrimaryTable();
        final SelectFinalStep<Record> query = jooq.select(getTableFields(fullFields)).from(primaryTable);
        final QueryExtension<SelectFinalStep<Record>> queryExtender = new EntitiesFetcher(jooq).queryExtender(query, primaryTable, parentKey, parentIds);
        final ResultQuery<Record> finalQuery = queryExtender.getQuery();
        return finalQuery.stream()
                .map(record -> readIdentifiers(record, parentKey, childKey))
                .onClose(() -> {
                    queryExtender.close();
                    query.close();
                });
    }

    private FullIdentifier<CHILD>
    readIdentifiers(Record record, UniqueKey<CHILD> parentKey, UniqueKey<CHILD> childKey) {
        return new FullIdentifier<>(fetchIdFromDB(parentKey, record), fetchIdFromDB(childKey, record));
    }

    private Identifier<CHILD>
    fetchIdFromDB(UniqueKey<CHILD> key, Record values) {
        final Object[] ids = Seq.of(key.getFields())
                .map(field -> {
                    final List<TableField<Record, ?>> tableFields = getTableField(field).collect(Collectors.toList());
                    final String id = String.valueOf(tableFields.get(0).get(values));
                    return field.getStringValueConverter().convertFrom(id);
                })
                .toArray();
        return new UniqueKeyValue<>(key, ids);
    }

    private UniqueKey<CHILD>
    getPartialChildKey(Collection<? extends ChangeEntityCommand<PARENT>> parents, CHILD childType) {
        final Optional<ChangeEntityCommand<CHILD>> childCommand = firstChild(parents, childType);
        return childCommand.map(changeEntityCommand -> changeEntityCommand.getIdentifier().getUniqueKey())
                .orElseGet(() -> getPartialChildKeyFromDBFields(first(parents).getEntityType(), childType));
    }

    private UniqueKey<CHILD>
    getPartialChildKeyFromDBFields(PARENT parentType, CHILD childType) {
        final EntityField<CHILD, ?>[] fullChildKey = childType.getPrimaryKey().getFields();
        final Collection<? extends EntityField<CHILD, ?>> childParentKey = childType.getKeyTo(parentType).from();
        return new UniqueKey<>(Seq.of(fullChildKey).filter(key -> !childParentKey.contains(key)).toList());
    }

    private Collection<TableField<Record, ?>>
    getTableFields(Collection<? extends EntityField<CHILD, ?>> fields) {
        return seq(fields)
                .flatMap(this::getTableField)
                .toList();
    }

    private Stream<TableField<Record, ?>>
    getTableField(EntityField<CHILD, ?> field) {
        return field.getDbAdapter().getTableFields();
    }

    private Optional<ChangeEntityCommand<CHILD>> firstChild(Collection<? extends ChangeEntityCommand<PARENT>> parents, CHILD childType) {
        return seq(parents).flatMap(parent -> parent.getChildren(childType)).findFirst();
    }

    private <T> T first(Collection<T> collection) {
        return collection.iterator().next();
    }
}
