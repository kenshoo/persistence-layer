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

    private OneToOnePlan oneToOnePlan;
    private List<ManyToOnePlan<?>> manyToOnePlans = Lists.newArrayList();


    public ExecutionPlan(DataTable startingTable, Collection<? extends EntityField<?, ?>> fieldsToFetch) {
        final Map<DataTable, ? extends List<? extends EntityField<?, ?>>> remainingPrimaryTables = targetTableToFieldsOf(fieldsToFetch, startingTable);

        final TreeEdge startingEdge = new TreeEdge(null, startingTable);
        final List<TreeEdge> oneToOnePaths = Lists.newArrayList();
        final List<? extends EntityField<?, ?>> oneToOneFields = Lists.newArrayList(fieldsToFetch);

        BFS.visit(startingEdge, this::edgesComingOutOf)
                .limitUntil(__ -> remainingPrimaryTables.isEmpty())
                .forEach(currentEdge -> {
                    final DataTable table = currentEdge.target.table;

                    List fields = remainingPrimaryTables.get(table);
                    if (currentEdge != startingEdge && fields != null) {
                        remainingPrimaryTables.remove(table);
                        oneToOnePaths.add(currentEdge);
                        calculatedAsMany(table).ifPresent(plan -> {
                            oneToOneFields.addAll(fields);
                            this.manyToOnePlans.remove(plan);
                        });
                    }
                    seq(remainingPrimaryTables).filter(referencing(table)).forEach(manyToOneEntry -> {
                        final TreeEdge sourceEdge = currentEdge == startingEdge ? null : currentEdge;
                        List<? extends EntityField<?, ?>> manyToOneFields = manyToOneEntry.v2;
                        oneToOneFields.removeAll(manyToOneFields);
                        populateManyToOnePlans(sourceEdge, table,  manyToOneEntry.v1, (List)manyToOneFields);
                    });
                });

        if (seq(remainingPrimaryTables.keySet()).anyMatch(notIn(this.manyToOnePlans))) {
            throw new IllegalStateException("Some tables " + remainingPrimaryTables + " could not be reached via joins");
        }

        this.oneToOnePlan = new OneToOnePlan(oneToOnePaths, oneToOneFields, oneToOneSecondaryTablesOf(fieldsToFetch));
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

    private Predicate<? super DataTable> notIn(List<ManyToOnePlan<?>> manyToOnePlans) {
        return table -> seq(manyToOnePlans).noneMatch(plan -> plan.getPath().target.table == table);
    }

    private Seq<TreeEdge> edgesComingOutOf(TreeEdge edge) {
        return seq(edge.target.table.getReferences()).map(new ToEdgesOf(edge.target));
    }

    private Optional<ManyToOnePlan<?>> calculatedAsMany(DataTable table) {
        return seq(this.manyToOnePlans).filter(plan -> plan.getPath().target.table == table).findFirst();
    }

    private <E extends EntityType<E>>  void populateManyToOnePlans(TreeEdge sourceEdge, DataTable targetTable, DataTable manyToOneTable , List<? extends EntityField<E, ?>> fields) {
        this.manyToOnePlans.add(new ManyToOnePlan<>(new TreeEdge(new TreeNode(sourceEdge, targetTable), manyToOneTable), fields));
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

