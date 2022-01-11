package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.ChangeEntityCommand;
import com.kenshoo.pl.entity.EntityType;

public class EntityCommandUtil {

    private EntityCommandUtil() {

    }

    public static ChangeEntityCommand getAncestor(ChangeEntityCommand cmd, EntityType level) {
        for (ChangeEntityCommand parent = cmd.getParent(); parent != null; parent = parent.getParent()) {
            if (parent.getEntityType().equals(level)) {
                return parent;
            }
        }
        throw new RuntimeException("didn't find ancestor of level " + level.getName() + " for command with entity " + cmd.getEntityType().getName());
    }
}
