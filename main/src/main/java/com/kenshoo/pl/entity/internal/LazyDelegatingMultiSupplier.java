package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.Entity;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.FieldsValueMap;
import com.kenshoo.pl.entity.spi.MultiFieldValueSupplier;
import com.kenshoo.pl.entity.spi.ValidationException;

import java.util.stream.Stream;

/**
 * Created by yuvalr on 2/15/16.
 */
public class LazyDelegatingMultiSupplier<E extends EntityType<E>> implements MultiFieldValueSupplier<E> {

    private final MultiFieldValueSupplier<E> multiSupplier;
    private FieldsValueMap<E> suppliedMap;

    public LazyDelegatingMultiSupplier(MultiFieldValueSupplier<E> multiSupplier) {
        this.multiSupplier = multiSupplier;
    }

    @Override
    public FieldsValueMap<E> supply(Entity currentState) throws ValidationException {
        if (suppliedMap == null) {
            suppliedMap = multiSupplier.supply(currentState);
        }
        return suppliedMap;
    }

    @Override
    public Stream<EntityField<?, ?>> fetchFields(ChangeOperation changeOperation) {
        return multiSupplier.fetchFields(changeOperation);
    }
}
