package com.kenshoo.pl.entity.spi.helpers;

import com.google.common.collect.Sets;
import com.kenshoo.pl.entity.ChangeEntityCommand;
import com.kenshoo.pl.entity.ChangeResult;
import com.kenshoo.pl.entity.CurrentEntityState;
import com.kenshoo.pl.entity.EntityChangeResult;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.FieldsValueMap;
import com.kenshoo.pl.entity.Identifier;
import com.kenshoo.pl.entity.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ChangeResultInspector<E extends EntityType<E>> {

    private static final Logger logger = LoggerFactory.getLogger(ChangeResultInspector.class);
    public static final String VALUE_NOT_FOUND = "not found";
    public static final ValidationError UNEXPECTED_ERROR = new ValidationError("Unexpected error");

    private final Set<EntityField<E, ?>> inspectedFields;
    private final String inspectedFlow;

    public ChangeResultInspector(Set<EntityField<E, ?>> inspectedFields, String inspectedFlow) {
        this.inspectedFields = inspectedFields;
        this.inspectedFlow = inspectedFlow;
    }

    public void inspect(Map<? extends Identifier<E>, CurrentEntityState> originalState, ChangeResult<E, ?, ?> results, List<ObservedResult<E>> observedResults) {
        Iterator<? extends EntityChangeResult<E, ?, ?>> resultIterator = results.iterator();
        Iterator<ObservedResult<E>> observedResultIterator = observedResults.iterator();
        while(resultIterator.hasNext() && observedResultIterator.hasNext()) {
            EntityChangeResult<E, ?, ?> result = resultIterator.next();
            ObservedResult<E> observedResult = observedResultIterator.next();
            if(result.isSuccess() && observedResult.isSuccess()) {
                inspectResult(originalState.get(result.getIdentifier()), result, observedResult);
            } else if(result.isSuccess()){
                logResultWarning(observedResult.getIdentifier(), observedResult.getErrorCode().orElse(""), true, false);
                observedResult.setInspectedStatus(ObservedResult.InspectedStatus.LEGACY_ERROR_MISMATCH);
            } else if(observedResult.isSuccess()) {
                Optional<ValidationError> firstError = result.getErrors().stream().findAny();
                logResultWarning(observedResult.getIdentifier(), firstError.orElse(UNEXPECTED_ERROR).getErrorCode(), false, true);
                observedResult.setInspectedStatus(ObservedResult.InspectedStatus.PERSISTENCE_ERROR_MISMATCH);
            }
        }
    }

    public static void logException(String inspectedFlow, Throwable t) {
        logger.warn("Change result inspector can't inspect keyword changes for flow " + inspectedFlow, t);
    }

    private void inspectResult(CurrentEntityState originalEntity, EntityChangeResult<E, ?, ?> result, ObservedResult<E> observedResult) {
        ChangeEntityCommand<E> command = result.getCommand();
        inspectedFields.forEach(field -> {
            boolean fieldChanged = command.isFieldChanged(field);
            boolean containsObservedField = observedResult.containsField(field);
            if(fieldChanged && containsObservedField) {
                Object value = getValue(command, field);
                Object observedValue = getValue(observedResult, field);
                if(!Objects.equals(value, observedValue)) {
                    logFieldWarning(observedResult.getIdentifier(), field.toString(), value, observedValue);
                    observedResult.setInspectedStatus(ObservedResult.InspectedStatus.VALUE_MISMATCH);
                }
            } else if(fieldChanged) {
                Object value = getValue(command, field);
                logFieldWarning(observedResult.getIdentifier(), field.toString(), value, VALUE_NOT_FOUND);
                observedResult.setInspectedStatus(ObservedResult.InspectedStatus.VALUE_MISMATCH);
            } else if(containsObservedField) {
                Object originalValue = originalEntity.containsField(field) ? getValue(originalEntity, field) : null;
                Object observedValue = getValue(observedResult, field);
                if(!Objects.equals(originalValue, observedValue)) {
                    logFieldWarning(observedResult.getIdentifier(), field.toString(), VALUE_NOT_FOUND, observedValue);
                    observedResult.setInspectedStatus(ObservedResult.InspectedStatus.VALUE_MISMATCH);
                }
            }
        });
    }


    private <T> Object getValue(CurrentEntityState fieldsValueMap, EntityField<E, T> field) {
        return field.getDbAdapter().getDbValues(fieldsValueMap.get(field)).collect(Collectors.toList()).get(0);
    }

    private <T> Object getValue(FieldsValueMap<E> fieldsValueMap, EntityField<E, T> field) {
        return field.getDbAdapter().getDbValues(fieldsValueMap.get(field)).collect(Collectors.toList()).get(0);
    }

    private void logFieldWarning(Identifier<E> identifier, String fieldName, Object value, Object observedValue) {
        logger.warn("Change result inspector reports different value result for flow {} and identifier {} for field {} : legacy {}, persistent {}", inspectedFlow, identifier.toString(), fieldName, observedValue, value);
    }

    private void logResultWarning(Identifier<E> identifier, String errorCode, boolean success, boolean inspectedSuccess) {
        logger.warn("Change result inspector reports validation mismatch for flow {} and identifier {} : legacy result [{}], persistent result [{}], error {}", inspectedFlow, identifier.toString(), inspectedSuccess, success, errorCode);
    }

    public static class Builder<E extends EntityType<E>> {
        private Set<EntityField<E, ?>> inspectedFields = Sets.newHashSet();
        private String inspectedFlow;


        public final Builder<E> withInspectedFields(Collection<EntityField<E, ?>> fields) {
            inspectedFields.addAll(fields);
            return this;
        }

        public Builder<E> inspectedFlow(String inspectedFlow) {
            this.inspectedFlow = inspectedFlow;
            return this;
        }

        public ChangeResultInspector<E> build() {
            return new ChangeResultInspector<>(inspectedFields, inspectedFlow);
        }
    }
}