package com.kenshoo.pl.entity.internal.fetch;

import com.google.common.collect.Lists;
import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.EntityField;
import org.jooq.lambda.Seq;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toSet;
import static org.jooq.lambda.Seq.seq;

public class FetchExecutionPlan {

    public Result run(DataTable startingTable, Collection<? extends EntityField<?, ?>> fieldsToFetch) {
        final Set<DataTable> targetTables = targetPrimaryTablesOf(fieldsToFetch, startingTable);

        final TreeEdge startingEdge = new TreeEdge(null, startingTable);
        final List<TreeEdge> manyToOneGraph = Lists.newArrayList();
        final List<TreeEdge> OneToOneGraph = Lists.newArrayList();

        BFS.visit(startingEdge, this::edgesComingOutOf)
                .limitUntil(__ -> targetTables.isEmpty())
                .forEach(currentEdge -> {
                    final DataTable table = currentEdge.target.table;

                    if (currentEdge != startingEdge && targetTables.contains(table)) {
                        targetTables.remove(table);
                        OneToOneGraph.add(currentEdge);
                        manyToOneGraph.removeIf(edge -> edge.target.table == table);
                    }
                    seq(targetTables).filter(referencing(table)).forEach(manyToOneTable -> {
                        final TreeEdge sourceEdge = currentEdge == startingEdge ? null : currentEdge;
                        manyToOneGraph.add(new TreeEdge(new TreeNode(sourceEdge, table), manyToOneTable));
                    });
                });

        if (seq(targetTables).anyMatch(notIn(manyToOneGraph))) {
            throw new IllegalStateException("Some tables " + targetTables + " could not be reached via joins");
        }

        return new Result(OneToOneGraph, manyToOneGraph);
    }

    private Set<DataTable> targetPrimaryTablesOf(Collection<? extends EntityField<?, ?>> fieldsToFetch, DataTable startingTable) {
        return fieldsToFetch.stream()
                .map(field -> field.getEntityType().getPrimaryTable())
                .filter(tb -> !tb.equals(startingTable))
                .collect(toSet());
    }

    private Predicate<? super DataTable> referencing(DataTable table) {
        return targetTable -> targetTable.getReferencesTo(table).size() == 1;
    }

    private Predicate<? super DataTable> notIn(List<TreeEdge> manyToOneGraph) {
        return table -> seq(manyToOneGraph).noneMatch(edge -> edge.target.table == table);
    }

    private Seq<TreeEdge> edgesComingOutOf(TreeEdge edge) {
        return seq(edge.target.table.getReferences()).map(new ToEdgesOf(edge.target));
    }

    class Result {
        private final List<TreeEdge> oneToOneGraph;
        private final List<TreeEdge> manyToOneGraph;

        public Result(List<TreeEdge> oneToOneGraph, List<TreeEdge> manyToOneGraph) {
            this.oneToOneGraph = oneToOneGraph;
            this.manyToOneGraph = manyToOneGraph;
        }

        public List<TreeEdge> getOneToOneGraph() {
            return oneToOneGraph;
        }

        public List<TreeEdge> getManyToOneGraph() {
            return manyToOneGraph;
        }
    }
}
