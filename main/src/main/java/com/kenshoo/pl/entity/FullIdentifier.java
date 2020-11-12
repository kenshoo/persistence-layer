package com.kenshoo.pl.entity;

import java.util.Objects;

public class FullIdentifier<PARENT extends EntityType<PARENT>, CHILD extends EntityType<CHILD>>{

    private final Identifier<PARENT> parentId;
    private final Identifier<CHILD> childId;
    private final Identifier<CHILD> keyToParent;

    public FullIdentifier(Identifier<PARENT> parentId, Identifier<CHILD> childId, Identifier<CHILD> keyToParent) {
        this.parentId = parentId;
        this.childId = childId;
        this.keyToParent = keyToParent;
    }

    public Identifier<PARENT> getParentId() {
        return parentId;
    }

    public Identifier<CHILD> getChildId() {
        return childId;
    }

    public Identifier<CHILD> getKetToParent() {
        return keyToParent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FullIdentifier<?, ?> that = (FullIdentifier<?, ?>) o;
        return Objects.equals(parentId, that.parentId) &&
                Objects.equals(childId, that.childId) &&
                Objects.equals(keyToParent, that.keyToParent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentId, childId, keyToParent);
    }

    @Override
    public String toString() {
        return "FullIdentifier{" +
                "parentId=" + parentId +
                ", childId=" + childId +
                '}';
    }
}
