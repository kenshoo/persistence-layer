package com.kenshoo.pl.entity.spi;

import com.kenshoo.pl.entity.EntityType;
import com.kenshoo.pl.entity.SupportedChangeOperation;

/**
 * Created by yuvalr on 1/18/16.
 */
public interface ChangeOperationSpecificConsumer<E extends EntityType<E>> extends CurrentStateConsumer<E> {

    SupportedChangeOperation getSupportedChangeOperation();

}
