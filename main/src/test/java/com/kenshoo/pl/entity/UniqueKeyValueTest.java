package com.kenshoo.pl.entity;

import org.jooq.lambda.Seq;
import org.junit.Test;

import static com.kenshoo.pl.entity.TestEntity.*;
import static com.kenshoo.pl.entity.UniqueKeyValue.concat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class UniqueKeyValueTest {

    @Test
    public void test_concat_ids_removes_duplicate_fields() {
        Identifier<TestEntity> id1 = identifier(key(FIELD_1, FIELD_2), "1", "2");
        Identifier<TestEntity> id2 = identifier(key(FIELD_3, FIELD_2), 3, "2");

        Identifier<TestEntity> merged = concat(id1, id2);

        assertThat(merged, is(identifier(key(FIELD_1, FIELD_2, FIELD_3), "1", "2", 3)));
    }

    @Test
    public void test_id_concatenated_to_itself_equals_itself() {
        Identifier<TestEntity> id = identifier(key(FIELD_1), "1");
        assertThat(concat(id, id), is(id));
    }

    private UniqueKey<TestEntity> key(EntityField<TestEntity, ?>... fields) {
        return new UniqueKey<>(Seq.of(fields));
    }

    private Identifier<TestEntity> identifier(UniqueKey<TestEntity> key, Object... values) {
        return new UniqueKeyValue<>(key, values);
    }
}
