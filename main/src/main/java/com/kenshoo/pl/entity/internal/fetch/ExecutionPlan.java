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

public class ExecutionPlan {

    private final List<TreeEdge> oneToOnePaths;
    private final List<TreeEdge> manyToOnePaths;

    public ExecutionPlan(DataTable startingTable, Collection<? extends EntityField<?, ?>> fieldsToFetch) {
        final Set<DataTable> targetTables = targetPrimaryTablesOf(fieldsToFetch, startingTable);

        final TreeEdge startingEdge = new TreeEdge(null, startingTable);
        final List<TreeEdge> manyToOnePaths = Lists.newArrayList();
        final List<TreeEdge> oneToOnePaths = Lists.newArrayList();

        BFS.visit(startingEdge, this::edgesComingOutOf)
                .limitUntil(__ -> targetTables.isEmpty())
                .forEach(currentEdge -> {
                    final DataTable table = currentEdge.target.table;

                    if (currentEdge != startingEdge && targetTables.contains(table)) {
                        targetTables.remove(table);
                        oneToOnePaths.add(currentEdge);
                        manyToOnePaths.removeIf(edge -> edge.target.table == table);
                    }
                    seq(targetTables).filter(referencing(table)).forEach(manyToOneTable -> {
                        final TreeEdge sourceEdge = currentEdge == startingEdge ? null : currentEdge;
                        manyToOnePaths.add(new TreeEdge(new TreeNode(sourceEdge, table), manyToOneTable));
                    });
                });

        if (seq(targetTables).anyMatch(notIn(manyToOnePaths))) {
            throw new IllegalStateException("Some tables " + targetTables + " could not be reached via joins");
        }

        this.oneToOnePaths= oneToOnePaths;
        this.manyToOnePaths= manyToOnePaths;
    }

    public List<TreeEdge> getOneToOnePaths() {
        return oneToOnePaths;
    }

    public List<TreeEdge> getManyToOnePaths() {
        return manyToOnePaths;
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

    private Predicate<? super DataTable> notIn(List<TreeEdge> manyToOnePaths) {
        return table -> seq(manyToOnePaths).noneMatch(edge -> edge.target.table == table);
    }

    private Seq<TreeEdge> edgesComingOutOf(TreeEdge edge) {
        return seq(edge.target.table.getReferences()).map(new ToEdgesOf(edge.target));
    }
}
