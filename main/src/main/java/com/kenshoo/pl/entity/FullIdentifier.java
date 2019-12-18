package com.kenshoo.pl.entity;

public class FullIdentifier<CHILD extends EntityType<CHILD>>{

    private final Identifier<CHILD> parentId;
    private final Identifier<CHILD> childId;

    public FullIdentifier(Identifier<CHILD> parentId, Identifier<CHILD> childId) {
        this.parentId = parentId;
        this.childId = childId;
    }

    public Identifier<CHILD> getParentId() {
        return parentId;
    }

    public Identifier<CHILD> getChildId() {
        return childId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FullIdentifier<?> that = (FullIdentifier<?>) o;

        if (parentId != null ? !parentId.equals(that.parentId) : that.parentId != null) return false;
        return childId != null ? childId.equals(that.childId) : that.childId == null;
    }

    @Override
    public int hashCode() {
        int result = parentId != null ? parentId.hashCode() : 0;
        result = 31 * result + (childId != null ? childId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "FullIdentifier{" +
                "parentId=" + parentId +
                ", childId=" + childId +
                '}';
    }
}
