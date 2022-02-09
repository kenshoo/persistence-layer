package com.kenshoo.pl.one2many.validators;

import com.kenshoo.jooq.DataTableUtils;
import com.kenshoo.jooq.TestJooqConfig;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.validators.RequiredAtLeastOneChildValidator;
import com.kenshoo.pl.one2many.relatedByPK.ChildTable;
import com.kenshoo.pl.one2many.relatedByPK.ParentTable;
import org.jooq.DSLContext;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static java.util.Collections.emptyMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.jooq.lambda.Seq.seq;
import static org.junit.Assert.fail;

public class RequiredAtLeastOneChildValidatorTest {

    private final static ParentTable PARENT = ParentTable.INSTANCE;
    private final static ChildTable CHILD = ChildTable.INSTANCE;

    private DSLContext jooq = TestJooqConfig.create();

    private PLContext plContext;

    private PersistenceLayer<TestEntity> persistenceLayer;


    @Before
    public void setUp() {
        jooq = TestJooqConfig.create();
        plContext = new PLContext.Builder(jooq).build();
        persistenceLayer = new PersistenceLayer<>(jooq);
        DataTableUtils.createTable(jooq, PARENT);
        DataTableUtils.createTable(jooq, CHILD);
    }

    @After
    public void clearTables() {
        jooq.deleteFrom(PARENT).execute();
        jooq.deleteFrom(CHILD).execute();
    }

    @Test
    public void plReturnsNoErrorWhenChildCommandIsPresented() {
        CreateEntityCommand<TestEntity> command = new CreateEntityCommand<>(TestEntity.INSTANCE) {{
            set(TestEntity.ID, 11);
            set(TestEntity.FIELD_1, "f1");
            addChild(new CreateEntityCommand<>(TestChildEntity.INSTANCE) {{
                set(TestChildEntity.CHILD_FIELD_1, "cf1");
            }});
        }};

        var result = persistenceLayer.create(List.of(command),
                defaultFlowConfig()
                        .withValidator(new RequiredAtLeastOneChildValidator<>(TestChildEntity.INSTANCE))
                        .build());

        assertThat(result.getErrors(command), emptyCollectionOf(ValidationError.class));
    }

    @Test
    public void plReturnsErrorWhenChildCommandIsNotPresented() {
        CreateEntityCommand<TestEntity> command = new CreateEntityCommand<>(TestEntity.INSTANCE) {{
            set(TestEntity.ID, 11);
            set(TestEntity.FIELD_1, "f1");
        }};

        var result = persistenceLayer.create(List.of(command),
                defaultFlowConfig()
                        .withValidator(new RequiredAtLeastOneChildValidator<>(TestChildEntity.INSTANCE))
                        .build());

        seq(result.getErrors(command))
                .findSingle()
                .ifPresentOrElse(error -> {
                    Assert.assertThat(error.getErrorCode(), is("At least one testChildEntity is required."));
                    Assert.assertThat(error.getField(), nullValue());
                    Assert.assertThat(error.getParameters(), is(emptyMap()));
                }, () -> fail("ValidationError is not presented"));

    }


    private ChangeFlowConfig.Builder<TestEntity> defaultFlowConfig() {
        return ChangeFlowConfigBuilderFactory.newInstance(plContext, TestEntity.INSTANCE)
                .withValidator(new RequiredAtLeastOneChildValidator<>(TestChildEntity.INSTANCE))
                .withChildFlowBuilder(ChangeFlowConfigBuilderFactory.newInstance(plContext, TestChildEntity.INSTANCE));
    }


}
