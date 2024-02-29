package com.kenshoo.pl.entity.spi;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.TestEntity;
import com.kenshoo.pl.entity.ValidationError;
import com.kenshoo.pl.entity.internal.FieldsCombination;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;


@RunWith(MockitoJUnitRunner.class)
public class FieldsCombinationValidatorTest {

    private final FieldsCombinationValidator<TestEntity> validator = new FieldsCombinationValidator<>() {

        @Override
        public Stream<EntityField<TestEntity, ?>> validatedFields() {
            return null;
        }

        @Override
        public ValidationError validate(FieldsCombination<TestEntity> fieldsValueMap) {
            return null;
        }
    };


    @Test
    public void trueForNullInput() {
        assertTrue(validator.validateWhen().test(null));
    }

    @Test
    public void noFieldToFetch() {
        assertTrue(validator.fetchFields().count() == 0);
    }
}