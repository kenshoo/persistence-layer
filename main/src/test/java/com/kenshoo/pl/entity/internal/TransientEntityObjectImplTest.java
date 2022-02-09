package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.TestEntityWithTransient;
import org.junit.Test;

import static com.kenshoo.pl.entity.TestEntityWithTransient.TRANSIENT_1;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TransientEntityObjectImplTest {

    @Test
    public void getEntityTypeShouldReturnCorrectOne() {
        assertThat(TRANSIENT_1.getEntityType(), is(TestEntityWithTransient.INSTANCE));
    }

    @Test
    public void getNameShouldReturnCorrectOne() {
        assertThat(TRANSIENT_1.getName(), is("transient_1"));
    }

    @Test
    public void toStringShouldReturnName() {
        assertThat(TRANSIENT_1.toString(), is("transient_1"));
    }
}