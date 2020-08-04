package com.kenshoo.pl.entity.internal.fetch;

import com.google.common.collect.Lists;
import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;

import java.util.*;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toSet;
import static org.jooq.lambda.Seq.seq;
import static org.jooq.lambda.function.Functions.not;

public class ExecutionPlan {

    private final OneToOnePlan oneToOnePlan;
    private final List<ManyToOnePlan<?>> manyToOnePlans;


    public ExecutionPlan(DataTable startingTable, Collection<? extends EntityField<?, ?>> fieldsToFetch) {
        final Map<DataTable, ? extends List<? extends EntityField<?, ?>>> remainingPrimaryTables = targetTableToFieldsOf(fieldsToFetch, startingTable);

        final TreeEdge startingEdge = new TreeEdge(null, startingTable);
        final List<TreeEdge> oneToOnePaths = Lists.newArrayList();
        final List<ManyToOnePlan<?>> manyToOneCandidates = Lists.newArrayList();

        BFS.visit(startingEdge, this::edgesComingOutOf)
                .limitUntil(__ -> remainingPrimaryTables.isEmpty())
                .forEach(currentEdge -> {
                    final DataTable table = currentEdge.target.table;

                    List fields = remainingPrimaryTables.get(table);
                    if (currentEdge != startingEdge && fields != null) {
                        remainingPrimaryTables.remove(table);
                        oneToOnePaths.add(currentEdge);
                    }
                    seq(remainingPrimaryTables).filter(referencing(table)).forEach(manyToOneEntry -> {
                        final TreeEdge sourceEdge = currentEdge == startingEdge ? null : currentEdge;
                        manyToOneCandidates.add(new ManyToOnePlan(new TreeEdge(new TreeNode(sourceEdge, table), manyToOneEntry.v1), manyToOneEntry.v2));
                    });
                });

        if (seq(remainingPrimaryTables.keySet()).anyMatch(notMany(manyToOneCandidates))) {
            throw new IllegalStateException("Some tables " + remainingPrimaryTables + " could not be reached via joins");
        }

        final List<? extends EntityField<?, ?>> oneToOneFields = seq(fieldsToFetch).filter(tableIn(oneToOnePaths).or(tableEqual(startingTable))).toList();
        this.oneToOnePlan = new OneToOnePlan(oneToOnePaths, oneToOneFields, oneToOneSecondaryTablesOf(fieldsToFetch));
        this.manyToOnePlans = seq(manyToOneCandidates).filter(notIn(oneToOnePaths)).toList();
    }

    public OneToOnePlan getOneToOnePlan() {
        return oneToOnePlan;
    }

    public List<ManyToOnePlan<?>> getManyToOnePlans() {
        return manyToOnePlans;
    }


    private Map<DataTable, ? extends List<? extends EntityField<?, ?>>> targetTableToFieldsOf(Collection<? extends EntityField<?, ?>> fieldsToFetch, DataTable startingTable) {
        return seq(fieldsToFetch)
                .filter(field -> !field.getEntityType().getPrimaryTable().equals(startingTable))
                .groupBy(this::parimaryTableOf);
    }

    private Set<OneToOneTableRelation> oneToOneSecondaryTablesOf(Collection<? extends EntityField<?, ?>> fields) {
        return fields.stream()
                .filter(not(isOfPrimaryTable()))
                .map(field -> com.kenshoo.pl.entity.internal.fetch.OneToOneTableRelation.builder()
                        .secondary(field.getDbAdapter().getTable())
                        .primary(field.getEntityType().getPrimaryTable())
                        .build())
                .collect(toSet());
    }

    private Predicate<EntityField<?, ?>> isOfPrimaryTable() {
        return field -> field.getDbAdapter().getTable().equals(field.getEntityType().getPrimaryTable());
    }

    private DataTable parimaryTableOf(EntityField<?, ?> field) {
        return field.getEntityType().getPrimaryTable();
    }

    private Predicate<? super Tuple2<DataTable, ? extends List<? extends EntityField<?, ?>>>> referencing(DataTable table) {
        return entry -> entry.v1.getReferencesTo(table).size() == 1;
    }

    private Predicate<EntityField<?,?>> tableIn(List<TreeEdge> oneToOnePaths) {
        return field -> seq(oneToOnePaths).anyMatch(path -> path.target.table == field.getEntityType().getPrimaryTable());
    }

    private Predicate<EntityField<?,?>> tableEqual(DataTable table) {
        return field -> field.getEntityType().getPrimaryTable() == table;
    }

    private Predicate<DataTable> notMany(List<ManyToOnePlan<?>> manyToOnePlans) {
        return table -> seq(manyToOnePlans).noneMatch(plan -> plan.getPath().target.table == table);
    }

    private Predicate<ManyToOnePlan<?>> notIn(List<TreeEdge> oneToOnePaths) {
        return plan -> seq(oneToOnePaths).noneMatch(path -> path.target.table == plan.getPath().target.table);
    }

    private Seq<TreeEdge> edgesComingOutOf(TreeEdge edge) {
        return seq(edge.target.table.getReferences()).map(new ToEdgesOf(edge.target));
    }

    public static class ManyToOnePlan<E extends EntityType<E>> {
        private final TreeEdge path;
        private final List<? extends EntityField<E, ?>> fields;

        ManyToOnePlan(TreeEdge path, List<? extends EntityField<E, ?>> fields) {
            this.path = path;
            this.fields = fields;
        }

        public TreeEdge getPath() {
            return path;
        }

        public List<? extends EntityField<E, ?>> getFields() {
            return fields;
        }
    }

    public static class OneToOnePlan {
        private final List<TreeEdge> paths;
        private final List<? extends EntityField<?, ?>> fields;
        private final Set<OneToOneTableRelation> secondaryTableRelations;

        OneToOnePlan(List<TreeEdge> paths, List<? extends EntityField<?, ?>> fields, Set<OneToOneTableRelation> secondaryTableRelations) {
            this.paths = paths;
            this.fields = fields;
            this.secondaryTableRelations = secondaryTableRelations;
        }

        public List<TreeEdge> getPaths() {
            return paths;
        }

        public List<? extends EntityField<?, ?>> getFields() {
            return fields;
        }

        public Set<OneToOneTableRelation> getSecondaryTableRelations() {
            return secondaryTableRelations;
        }
    }
}

