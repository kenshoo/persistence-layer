package com.kenshoo.pl.entity;

import org.junit.Test;

import java.util.Optional;

import static com.kenshoo.pl.entity.TestEntityWithTransient.TRANSIENT_1;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ChangeEntityCommandTest {

    private final CreateEntityCommand<TestEntityWithTransient> cmdWithTransient =
            new CreateEntityCommand<>(TestEntityWithTransient.INSTANCE);

    @Test
    public void getTransientObjectWhenDoesntExistInCommandShouldReturnEmpty() {
        assertThat(cmdWithTransient.get(TRANSIENT_1), is(Optional.empty()));
    }

    @Test
    public void setAndGetTransientObjectReturnsCorrectValue() {
        final String transientVal = "transientVal";

        final var cmd = new CreateEntityCommand<>(TestEntityWithTransient.INSTANCE);
        cmd.set(TRANSIENT_1, transientVal);

        assertThat(cmd.get(TRANSIENT_1), is(Optional.of(transientVal)));
    }

    @Test
    public void setTwiceAndGetTransientObjectReturnsCorrectValue() {
        final String transientVal1 = "transientVal1";
        final String transientVal2 = "transientVal2";

        final var cmd = new CreateEntityCommand<>(TestEntityWithTransient.INSTANCE);
        cmd.set(TRANSIENT_1, transientVal1);
        cmd.set(TRANSIENT_1, transientVal2);

        assertThat(cmd.get(TRANSIENT_1), is(Optional.of(transientVal2)));
    }

    @Test(expected = NullPointerException.class)
    public void setTransientObjectToNullShouldThrowException() {
        cmdWithTransient.set(TRANSIENT_1, null);
    }
}