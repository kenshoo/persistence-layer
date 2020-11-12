package com.kenshoo.pl.entity.internal.fetch;

import com.kenshoo.jooq.DataTable;
import org.jooq.ForeignKey;
import org.jooq.Record;

import java.util.function.Function;

public  class ToEdgesOf implements Function<ForeignKey<Record, ?>, TreeEdge> {

    private final TreeNode node;

    public ToEdgesOf(TreeNode node) {
        this.node = node;
    }

    @Override
    public TreeEdge apply(ForeignKey<Record, ?> foreignKey) {
        return new TreeEdge(node, (DataTable) foreignKey.getTable());
    }

}
