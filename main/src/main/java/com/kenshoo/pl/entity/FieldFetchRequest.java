package com.kenshoo.pl.entity;

import java.util.Objects;

public class FieldFetchRequest {

    private final EntityType whereToQuery;
    private final EntityType whoAskedForThis;
    private final EntityField<?,?> entityField;

    FieldFetchRequest(EntityType whereToQuery, EntityType whoAskedForThis, EntityField<?, ?> entityField) {
        this.whereToQuery = whereToQuery;
        this.whoAskedForThis = whoAskedForThis;
        this.entityField = entityField;
    }

    public EntityType getWhereToQuery() {
        return whereToQuery;
    }

    public EntityType getWhoAskedForThis() {
        return whoAskedForThis;
    }

    public EntityField<?, ?> getEntityField() {
        return entityField;
    }

    public boolean supports(ChangeOperation changeOperation) {
        return getEntityField().getEntityType() != whereToQuery || changeOperation != ChangeOperation.CREATE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldFetchRequest that = (FieldFetchRequest) o;
        return Objects.equals(whereToQuery, that.whereToQuery) &&
                Objects.equals(whoAskedForThis, that.whoAskedForThis) &&
                Objects.equals(entityField, that.entityField);
    }

    @Override
    public int hashCode() {
        return Objects.hash(whereToQuery, whoAskedForThis, entityField);
    }

    public static Builder newRequest() {
        return new Builder();
    }

    public static class Builder {
        private EntityType whereToQuery;
        private EntityType whoAskedForThis;
        private EntityField<?,?> entityField;

        public FieldFetchRequest build() {
            return new FieldFetchRequest(whereToQuery, whoAskedForThis, entityField);
        }

        public Builder queryOn(EntityType v) {
            this.whereToQuery = v;
            return this;
        }

        public Builder askedBy(EntityType v) {
            this.whoAskedForThis = v;
            return this;
        }


        public Builder field(EntityField v) {
            this.entityField = v;
            return this;
        }
    }

}
