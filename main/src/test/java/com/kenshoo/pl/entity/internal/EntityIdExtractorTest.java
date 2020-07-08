package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAndIs;
import static com.kenshoo.pl.entity.ChangeOperation.CREATE;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class EntityIdExtractorTest {

    private static final int ID_VALUE = 123;
    private static final String STRING_ID_VALUE = String.valueOf(ID_VALUE);

    @Test
    public void extractWhenExistsInCommandDirectly() {
        final EntityChange<TestEntity> cmd = new TestCommand()
            .with(TestEntity.ID, ID_VALUE);
        final CurrentEntityState currentState = CurrentEntityState.EMPTY;

        assertThat(EntityIdExtractor.INSTANCE.extract(cmd, currentState), isPresentAndIs(STRING_ID_VALUE));
    }

    @Test
    public void extractWhenExistsInIdentifierButNotDirectlyInCommand() {
        final EntityChange<TestEntity> cmd = new TestCommand()
            .withIdentifier(new TestEntity.Key(ID_VALUE));
        final CurrentEntityState currentState = CurrentEntityState.EMPTY;

        assertThat(EntityIdExtractor.INSTANCE.extract(cmd, currentState), isPresentAndIs(STRING_ID_VALUE));
    }

    @Test
    public void extractWhenExistsInEntityButNotInIdentifier() {
        final EntityChange<TestEntity> cmd = new TestCommand()
            .withIdentifier(new SingleUniqueKeyValue<>(TestEntity.FIELD_1, "abc"));
        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(TestEntity.ID, ID_VALUE);

        assertThat(EntityIdExtractor.INSTANCE.extract(cmd, currentState), isPresentAndIs(STRING_ID_VALUE));
    }

    @Test
    public void extractWhenExistsInEntityOnly() {
        final EntityChange<TestEntity> cmd = TestCommand.EMPTY;
        final CurrentEntityMutableState currentState = new CurrentEntityMutableState();
        currentState.set(TestEntity.ID, ID_VALUE);

        assertThat(EntityIdExtractor.INSTANCE.extract(cmd, currentState), isPresentAndIs(STRING_ID_VALUE));
    }

    @Test
    public void extractWhenDoesntExistAnywhere() {
        final EntityChange<TestEntity> cmd = TestCommand.EMPTY;
        final CurrentEntityState currentState = CurrentEntityState.EMPTY;

        assertThat(EntityIdExtractor.INSTANCE.extract(cmd, currentState), isEmpty());
    }

    private static class TestCommand extends ChangeEntityCommand<TestEntity> implements EntityCommandExt<TestEntity, TestCommand> {

        private static final TestCommand EMPTY = new TestCommand();

        private Identifier<TestEntity> identifier;

        private TestCommand() {
            super(TestEntity.INSTANCE);
        }

        private TestCommand withIdentifier(final Identifier<TestEntity> identifier) {
            this.identifier = identifier;
            return this;
        }

        @Override
        public Identifier<TestEntity> getIdentifier() {
            return identifier;
        }

        @Override
        public ChangeOperation getChangeOperation() {
            return CREATE;
        }
    }
}