package com.kenshoo.pl.entity.internal.fetch;

import com.google.common.collect.Sets;
import com.kenshoo.jooq.DataTable;
import com.kenshoo.jooq.QueryExtension;
import com.kenshoo.pl.entity.UniqueKey;
import com.kenshoo.pl.entity.*;
import org.jooq.*;

import java.util.*;

class QueryBuilder<E extends EntityType<E>> {

    private DSLContext dslContext;
    private QueryBuilderHelper queryBuilderHelper;
    private List<SelectField<?>> selectedFields;
    private DataTable startingTable;
    private List<TreeEdge> paths;
    private Set<OneToOneTableRelation> oneToOneTableRelations;
    private Collection<? extends Identifier<E>> ids;


    QueryBuilder(DSLContext dslContext) {
        this.dslContext = dslContext;
        this.queryBuilderHelper = new QueryBuilderHelper(dslContext);
    }

    QueryBuilder selecting(List<SelectField<?>> selectedFields) {
        this.selectedFields = selectedFields;
        return this;
    }

    QueryBuilder from(DataTable primaryTable) {
        this.startingTable = primaryTable;
        return this;
    }

    QueryBuilder innerJoin(List<TreeEdge> paths) {
        this.paths = paths;
        return this;
    }

    QueryBuilder innerJoin(TreeEdge path) {
        this.paths = List.of(path);
        return this;
    }

    QueryBuilder leftJoin(Set<OneToOneTableRelation> oneToOneTableRelations) {
        this.oneToOneTableRelations = oneToOneTableRelations;
        return this;
    }

    QueryBuilder whereIdsIn(Collection<? extends Identifier<E>> ids) {
        this.ids = ids;
        return this;
    }

    QueryExtension<SelectJoinStep> build() {
        final SelectJoinStep<Record> query = dslContext.select(selectedFields).from(startingTable);
        final Set<DataTable> joinedTables = Sets.newHashSet(startingTable);
        paths.forEach(edge -> queryBuilderHelper.joinTables(query, joinedTables, edge));
        if (oneToOneTableRelations != null) {
            queryBuilderHelper.joinSecondaryTables(query, joinedTables, oneToOneTableRelations);
        }
        final UniqueKey<E> uniqueKey = ids.iterator().next().getUniqueKey();
        return queryBuilderHelper.addIdsCondition(query, startingTable, uniqueKey, ids);
    }
}
