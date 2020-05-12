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
        final Map<DataTable, ? extends List<? extends EntityField<?, ?>>> targetTableToFieldsMap = targetTableToFieldsOf(fieldsToFetch, startingTable);

        final TreeEdge startingEdge = new TreeEdge(null, startingTable);
        final List<TreeEdge> oneToOnePaths = Lists.newArrayList();
        final List<? extends EntityField<?, ?>> oneToOneFields = Lists.newArrayList();

        BFS.visit(startingEdge, this::edgesComingOutOf)
                .limitUntil(__ -> targetTableToFieldsMap.isEmpty())
                .forEach(currentEdge -> {
                    final DataTable table = currentEdge.target.table;

                    List fields = targetTableToFieldsMap.get(table);
                    if (currentEdge != startingEdge && fields != null) {
                        targetTableToFieldsMap.remove(table);
                        oneToOnePaths.add(currentEdge);
                        oneToOneFields.addAll(fields);
                        this.manyToOnePlans.removeIf(plan -> plan.getPath().target.table == table);
                    }
                    seq(targetTableToFieldsMap).filter(referencing(table)).forEach(manyToOneEntry -> {
                        final TreeEdge sourceEdge = currentEdge == startingEdge ? null : currentEdge;
                        this.manyToOnePlans.add(new ManyToOnePlan(new TreeEdge(new TreeNode(sourceEdge, table), manyToOneEntry.v1), manyToOneEntry.v2));
                    });
                });

        if (seq(targetTableToFieldsMap.keySet()).anyMatch(notIn(this.manyToOnePlans))) {
            throw new IllegalStateException("Some tables " + targetTableToFieldsMap + " could not be reached via joins");
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
                .groupBy(this::tableOf);
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

    private DataTable tableOf(EntityField<?, ?> field) {
        return field.getEntityType().getPrimaryTable();
    }

    private Predicate<? super Tuple2<DataTable, ? extends List<? extends EntityField<?, ?>>>> referencing(DataTable table) {
        return entry -> entry.v1.getReferencesTo(table).size() == 1;
    }

    private Predicate<? super DataTable> notIn(List<ManyToOnePlan> manyToOnePlans) {
        return table -> seq(manyToOnePlans).noneMatch(plan -> plan.getPath().target.table == table);
    }

    private Seq<TreeEdge> edgesComingOutOf(TreeEdge edge) {
        return seq(edge.target.table.getReferences()).map(new ToEdgesOf(edge.target));
    }

    public static class ManyToOnePlan<SUB extends EntityType<SUB>> {
        private final TreeEdge path;
        private final List<? extends EntityField<SUB, ?>> fields;

        ManyToOnePlan(TreeEdge path, List<? extends EntityField<SUB, ?>> fields) {
            this.path = path;
            this.fields = fields;
        }

        public TreeEdge getPath() {
            return path;
        }

        public List<? extends EntityField<SUB, ?>> getFields() {
            return fields;
        }
    }

    public static class OneToOnePlan {
        private final List<TreeEdge> paths;
        private final List<? extends EntityField<?, ?>> fields;
        private final Set<OneToOneTableRelation> secondaryTableRelations;

        public OneToOnePlan(List<TreeEdge> paths, List<? extends EntityField<?, ?>> fields, Set<OneToOneTableRelation> secondaryTableRelations) {
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
