package com.kenshoo.pl.entity;


public interface TestDataFieldPrototype {

    EntityFieldPrototype<String> FIELD_1 = new EntityFieldPrototype<>("Field 1");
    EntityFieldPrototype<String> FIELD_2 = new EntityFieldPrototype<>("Field 2");
    EntityFieldPrototype<Integer> FIELD_3 = new EntityFieldPrototype<>("Field 2");
}
