package com.kenshoo.pl.entity.validators;

import com.kenshoo.pl.entity.*;
import org.junit.Test;

import java.util.List;

import static java.util.Collections.emptyMap;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class RequiredAtLeastOneChildValidatorTest {

    private final RequiredAtLeastOneChildValidator<TestEntity, TestChildEntity> onTest = new RequiredAtLeastOneChildValidator<>(TestChildEntity.INSTANCE);

    @Test
    public void returnNoErrorWhenChildCommandIsPresented() {
        CreateEntityCommand<TestEntity> command = new CreateEntityCommand<>(TestEntity.INSTANCE) {{
            set(TestEntity.FIELD_1, "field1");
            addChild(new CreateEntityCommand<>(TestChildEntity.INSTANCE) {{
                set(TestChildEntity.CHILD_FIELD_1, "child1");
            }});
        }};
        ChangeContext context = new ChangeContextImpl(null, FeatureSet.EMPTY);

        onTest.validate(List.of(command), ChangeOperation.CREATE, context);

        assertThat(context.hasValidationErrors(), is(false));
    }

    @Test
    public void returnErrorWhenChildCommandIsNotPresented() {
        CreateEntityCommand<TestEntity> command = new CreateEntityCommand<>(TestEntity.INSTANCE) {{
            set(TestEntity.FIELD_1, "field1");
        }};
        ChangeContext context = new ChangeContextImpl(null, FeatureSet.EMPTY);

        onTest.validate(List.of(command), ChangeOperation.CREATE, context);

        context.getValidationErrors(command)
                .findSingle()
                .ifPresentOrElse(error -> {
                    assertThat(error.getErrorCode(), is("At least one testChildEntity is required."));
                    assertThat(error.getField(), nullValue());
                    assertThat(error.getParameters(), is(emptyMap()));
                }, () -> fail("ValidationError is not presented"));
    }

    @Test
    public void returnSupportedChangeOperation() {
        assertThat(onTest.getSupportedChangeOperation(), is(SupportedChangeOperation.CREATE));
    }

}