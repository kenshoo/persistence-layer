package com.kenshoo.pl.entity;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.annotation.Id;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class TestEntityWithTransient extends AbstractEntityType<TestEntityWithTransient> {

    public static final TestEntityWithTransient INSTANCE = new TestEntityWithTransient();

    @Id
    public static final EntityField<TestEntityWithTransient, Integer> ID = INSTANCE.field(TestEntityTable.TABLE.id);
    public static final EntityField<TestEntityWithTransient, String> FIELD_1 = INSTANCE.field(TestEntityTable.TABLE.field_1);

    @DummyAnnotation
    public static final TransientEntityProperty<TestEntityWithTransient, String> TRANSIENT_1 = INSTANCE.transientProperty("transient_1");
    public static final TransientEntityProperty<TestEntityWithTransient, String> TRANSIENT_2 = INSTANCE.transientProperty("transient_2");

    private TestEntityWithTransient() {
        super("testWithTransient");
    }

    @Override
    public DataTable getPrimaryTable() {
        return TestEntityTable.TABLE;
    }


    @Retention(RetentionPolicy.RUNTIME)
    public @interface DummyAnnotation {}
}
