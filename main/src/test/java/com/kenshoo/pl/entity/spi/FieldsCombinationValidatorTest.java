package com.kenshoo.pl.entity.spi;

import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.FieldsValueMap;
import com.kenshoo.pl.entity.TestEntity;
import com.kenshoo.pl.entity.ValidationError;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;


@RunWith(MockitoJUnitRunner.class)
public class FieldsCombinationValidatorTest {

    private FieldsCombinationValidator<TestEntity> validator = new FieldsCombinationValidator<TestEntity>() {

        @Override
        public Stream<EntityField<TestEntity, ?>> validatedFields() {
            return null;
        }

        @Override
        public ValidationError validate(FieldsValueMap<TestEntity> fieldsValueMap) {
            return null;
        }
    };


    @Test
    public void trueForNullInput() {
        assertTrue(validator.validateWhen().test(null));
    }

    @Test
    public void noSubstitutions() {
        assertTrue(validator.substitutions().count() == 0);
    }

    @Test
    public void noFieldToFetch() {
        assertTrue(validator.fetchFields().count() == 0);
    }
}