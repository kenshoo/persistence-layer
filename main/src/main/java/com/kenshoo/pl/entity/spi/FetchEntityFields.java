package com.kenshoo.pl.entity.spi;

import com.kenshoo.pl.entity.ChangeOperation;
import com.kenshoo.pl.entity.EntityField;

import java.util.stream.Stream;

public interface FetchEntityFields {

    /**
     * @return a list of fields to fetch.
     */
    default Stream<EntityField<?, ?>> fetchFields(ChangeOperation changeOperation) {
        return Stream.empty();
    }

}
