package com.kenshoo.pl.entity.spi.helpers;


import com.kenshoo.pl.entity.Entity;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.FieldsValueMap;
import com.kenshoo.pl.entity.Identifier;

import java.util.Optional;

public class ObservedResult<E extends EntityType<E>> implements FieldsValueMap<E>{

    public enum InspectedStatus {
        IDENTICAL,
        VALUE_MISMATCH,
        LEGACY_ERROR_MISMATCH,
        PERSISTENCE_ERROR_MISMATCH
    }

    private final boolean isSuccess;
    private final Identifier<E> identifier;
    private final Entity entity;
    private final Optional<String> errorCode;
    private InspectedStatus inspectedStatus = InspectedStatus.IDENTICAL;


    private ObservedResult(Identifier<E> identifier, Entity entity) {
        this(identifier, entity, true, null);
    }

    private ObservedResult(Identifier<E> identifier, Entity entity, boolean isSuccess, String errorCode) {
        this.isSuccess = isSuccess;
        this.identifier = identifier;
        this.entity = entity;
        this.errorCode = Optional.ofNullable(errorCode);
    }

    public InspectedStatus getInspectedStatus() {
        return inspectedStatus;
    }

    void setInspectedStatus(InspectedStatus inspectedStatus) {
        this.inspectedStatus = inspectedStatus;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public Identifier<E> getIdentifier() {
        return identifier;
    }

    @Override
    public <T> boolean containsField(EntityField<E, T> field) {
        return entity.containsField(field);
    }

    @Override
    public <T> T get(EntityField<E, T> field) {
        return entity.get(field);
    }


    public Optional<String> getErrorCode() {
        return errorCode;
    }

    public static <E extends EntityType<E>> ObservedResult<E> of(Identifier<E> identifier, Entity entity)  {
        return new ObservedResult<>(identifier, entity);
    }

    public static <E extends EntityType<E>> ObservedResult<E> error(Identifier<E> identifier,  String errorCode)  {
        return new ObservedResult<>(identifier, null, false, errorCode);
    }

    public static class Builder<E extends EntityType<E>> {
        private Identifier<E> identifier;
        private Entity entity;
        private boolean isSuccess = true;
        private String errorCode;

        public Builder<E> withIdentifier(Identifier<E> identifier) {
            this.identifier = identifier;
            return this;
        }

        public Builder<E> withEntity(Entity entity) {
            this.entity = entity;
            return this;
        }

        public Builder<E> withErrorCode(String errorCode) {
            this.isSuccess = false;
            this.errorCode = errorCode;
            return this;
        }

        public ObservedResult<E> build() {
            return new ObservedResult<>(identifier, entity, isSuccess, errorCode);
        }
    }
}
