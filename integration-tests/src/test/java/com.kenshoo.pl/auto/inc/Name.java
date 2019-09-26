package com.kenshoo.pl.auto.inc;

import com.kenshoo.pl.entity.SingleUniqueKey;
import com.kenshoo.pl.entity.SingleUniqueKeyValue;

public class Name extends SingleUniqueKeyValue<TestEntity, String> {

    public static final SingleUniqueKey<TestEntity, String> DEFINITION = new SingleUniqueKey<TestEntity, String>(TestEntity.NAME) {
        @Override
        protected Name createValue(String value) {
            return new Name(value);
        }
    };

    public Name(String name) {
        super(DEFINITION, name);
    }

}
