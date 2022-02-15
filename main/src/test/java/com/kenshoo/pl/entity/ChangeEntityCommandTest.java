package com.kenshoo.pl.entity;

import com.kenshoo.pl.entity.internal.TransientPropertyImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ChangeEntityCommandTest {

    private final TransientProperty<String> transientProperty =  new TransientPropertyImpl<>("interesting description", String.class);

    private CreateEntityCommand<TestEntity> cmd;

    @Before
    public void setUp() {
        cmd = new CreateEntityCommand<>(TestEntity.INSTANCE);
    }

    @Test
    public void getTransientPropertyWhenDoesntExistShouldReturnEmpty() {
        assertThat(cmd.get(transientProperty), is(Optional.empty()));
    }

    @Test
    public void setAndGetTransientPropertyReturnsCorrectValue() {
        final String transientVal = "transientVal";

        cmd.set(transientProperty, transientVal);

        assertThat(cmd.get(transientProperty), is(Optional.of(transientVal)));
    }

    @Test
    public void setTwiceAndGetTransientPropertyReturnsCorrectValue() {
        final String transientVal1 = "transientVal1";
        final String transientVal2 = "transientVal2";

        cmd.set(transientProperty, transientVal1);
        cmd.set(transientProperty, transientVal2);

        assertThat(cmd.get(transientProperty), is(Optional.of(transientVal2)));
    }

    @Test(expected = NullPointerException.class)
    public void setTransientPropertyToNullShouldThrowException() {
        cmd.set(transientProperty, null);
    }
}