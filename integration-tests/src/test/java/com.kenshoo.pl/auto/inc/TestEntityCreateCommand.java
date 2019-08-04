package com.kenshoo.pl.auto.inc;

import com.kenshoo.pl.entity.CreateEntityCommand;

class TestEntityCreateCommand extends CreateEntityCommand<TestEntity> {

    private TestEntityCreateCommand() {
        super(TestEntity.INSTANCE);
    }

    static Builder builder() {
        return new Builder();
    }

    static final class Builder {

        private String name;
        private String secondName;

        Builder withName(final String name) {
            this.name = name;
            return this;
        }

        Builder withSecondName(final String secondName) {
            this.secondName = secondName;
            return this;
        }

        TestEntityCreateCommand build() {
            final TestEntityCreateCommand cmd = new TestEntityCreateCommand();
            cmd.set(TestEntity.NAME, name);
            cmd.set(TestEntity.SECOND_NAME, secondName);
            return cmd;
        }
    }
}
