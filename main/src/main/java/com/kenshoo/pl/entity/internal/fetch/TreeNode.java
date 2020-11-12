package com.kenshoo.pl.entity.internal.fetch;

import com.kenshoo.jooq.DataTable;

public class TreeNode {
    public final DataTable table;
    public final TreeEdge parent;

    public TreeNode(TreeEdge parent, DataTable table) {
        this.parent = parent;
        this.table = table;
    }
}
