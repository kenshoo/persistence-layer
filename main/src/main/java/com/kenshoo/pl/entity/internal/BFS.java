package com.kenshoo.pl.entity.internal;

import com.google.common.collect.TreeTraverser;
import org.jooq.lambda.Seq;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static java.util.Collections.singletonList;
import static org.jooq.lambda.Seq.seq;
import static org.jooq.lambda.function.Functions.not;

public class BFS {

    static <Tree> Seq<Tree> visit(Tree root, Function<Tree, Iterable<Tree>> neighbours) {

        TreeTraverser<Tree> visitor = new TreeTraverser<Tree>() {

            Set<Tree> alreadyVisited = new HashSet<>(singletonList(root));

            @Override
            public Iterable<Tree> children(Tree node) {
                List<Tree> unvisitedChildren = seq(neighbours.apply(node)).filter(not(alreadyVisited::contains)).toList();
                alreadyVisited.addAll(unvisitedChildren);
                return unvisitedChildren;
            }
        };

        return seq(visitor.breadthFirstTraversal(root));
    }

}
