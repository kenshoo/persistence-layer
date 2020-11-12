package com.kenshoo.pl.entity.internal.fetch;

import com.kenshoo.jooq.DataTable;

public class TreeEdge {
    public final TreeNode source;
    public final TreeNode target;

    public TreeEdge(TreeNode source, DataTable targetTable) {
        this.source = source;
        this.target = new TreeNode(this, targetTable);
    }
}
