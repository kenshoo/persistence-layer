package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.*;
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
    public FieldsValueMap<E> supply(Entity entity) throws ValidationException {
        if (suppliedMap == null) {
            suppliedMap = multiSupplier.supply(entity);
        }
        return suppliedMap;
    }

    @Override
    public Stream<EntityField<?, ?>> fetchFields(ChangeOperation changeOperation) {
        return multiSupplier.fetchFields(changeOperation);
    }
}
