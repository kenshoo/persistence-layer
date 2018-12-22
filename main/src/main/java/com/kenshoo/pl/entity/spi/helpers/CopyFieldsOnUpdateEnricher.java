package com.kenshoo.pl.entity.spi.helpers;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.SupportedChangeOperation;

public class CopyFieldsOnUpdateEnricher<E extends EntityType<E>> extends CopyFieldsOnCreateEnricher<E>  {


    public <T1, T2> CopyFieldsOnUpdateEnricher(EntityField<?, T1> sourceField1, EntityField<E, T1> targetField1,
                                                       EntityField<?, T2> sourceField2, EntityField<E, T2> targetField2) {
        super(sourceField1,targetField1,sourceField2,targetField2);
    }

    @Override
    protected void addRequiredFields() {
        super.addRequiredFields();

        this.fields2Copy.stream()
                .map(Field2Copy::getTarget)
                .forEach(requiredFields::add);
    }

    @Override
    public SupportedChangeOperation getSupportedChangeOperation() {
        return SupportedChangeOperation.UPDATE;
    }
}
