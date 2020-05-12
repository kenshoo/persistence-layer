package com.kenshoo.pl.entity.internal.fetch;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.kenshoo.jooq.DataTable;
import com.kenshoo.jooq.QueryExtension;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.UniqueKey;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SelectField;
import org.jooq.SelectJoinStep;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class QueryBuilder<E extends EntityType<E>> {

    private DSLContext dslContext;
    private QueryBuilderHelper queryBuilderHelper;
    private List<SelectField<?>> selectedFields;
    private DataTable startingTable;
    private List<TreeEdge> paths;
    private Set<OneToOneTableRelation> oneToOneTableRelations;
    private Collection<? extends Identifier<E>> ids;


    public QueryBuilder(DSLContext dslContext) {
        this.dslContext = dslContext;
        this.queryBuilderHelper = new QueryBuilderHelper(dslContext);
    }

    public QueryBuilder<E> selecting(List<SelectField<?>> selectedFields) {
        this.selectedFields = selectedFields;
        return this;
    }

    public QueryBuilder<E> from(DataTable primaryTable) {
        this.startingTable = primaryTable;
        return this;
    }

    public QueryBuilder<E> innerJoin(List<TreeEdge> paths) {
        this.paths = paths;
        return this;
    }

    public QueryBuilder<E> innerJoin(TreeEdge path) {
        this.paths = Lists.newArrayList(path);
        return this;
    }

    public QueryBuilder<E> leftJoin(Set<OneToOneTableRelation> oneToOneTableRelations) {
        this.oneToOneTableRelations = oneToOneTableRelations;
        return this;
    }

    public QueryBuilder<E> whereIdsIn(Collection<? extends Identifier<E>> ids) {
        this.ids = ids;
        return this;
    }

    public QueryExtension<SelectJoinStep<Record>> build() {
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
