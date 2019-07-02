package com.kenshoo.pl.entity;

import org.junit.Test;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by Yuval on 05/07/2016.
 */
public class CommonTypesStringConverterTest {

    @Test(expected = IllegalArgumentException.class)
    public void unsupportedClass() {
        new CommonTypesStringConverter<>(Entity.class);
    }

    @Test
    public void integerValid() {
        CommonTypesStringConverter<Integer> onTest = new CommonTypesStringConverter<>(Integer.class);

        testToAndFromString(onTest, 4);
    }

    @Test(expected = NumberFormatException.class)
    public void integerInvalid() {
        CommonTypesStringConverter<Integer> onTest = new CommonTypesStringConverter<>(Integer.class);

        onTest.convertFrom("4.4");
    }

    @Test
    public void longValid() {
        CommonTypesStringConverter<Long> onTest = new CommonTypesStringConverter<>(Long.class);

        testToAndFromString(onTest, 4l);
    }

    private <T> void testToAndFromString(CommonTypesStringConverter<T> onTest, T originalValue) {
        String asString = onTest.convertTo(originalValue);
        assertThat(onTest.convertFrom(asString), is(originalValue));
    }

    @Test(expected = NumberFormatException.class)
    public void longInvalid() {
        CommonTypesStringConverter<Long> onTest = new CommonTypesStringConverter<>(Long.class);

        onTest.convertFrom("-4.44");
    }

    @Test
    public void floatValid() {
        CommonTypesStringConverter<Float> onTest = new CommonTypesStringConverter<>(Float.class);

        testToAndFromString(onTest, 1.23456f);
    }

    @Test(expected = NumberFormatException.class)
    public void floatInvalid() {
        CommonTypesStringConverter<Float> onTest = new CommonTypesStringConverter<>(Float.class);

        onTest.convertFrom("abc");
    }

    @Test
    public void doubleValid() {
        CommonTypesStringConverter<Double> onTest = new CommonTypesStringConverter<>(Double.class);

        testToAndFromString(onTest, 1.23456d);
    }

    @Test(expected = NumberFormatException.class)
    public void doubleInvalid() {
        CommonTypesStringConverter<Double> onTest = new CommonTypesStringConverter<>(Double.class);

        onTest.convertFrom("zyx");
    }

    @Test
    public void booleanValid() {
        CommonTypesStringConverter<Boolean> onTest = new CommonTypesStringConverter<>(Boolean.class);

        testToAndFromString(onTest, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void booleanInvalid() {
        CommonTypesStringConverter<Boolean> onTest = new CommonTypesStringConverter<>(Boolean.class);

        onTest.convertFrom("garbage");
    }

    @Test
    public void bigDecimalValid() {
        CommonTypesStringConverter<BigDecimal> onTest = new CommonTypesStringConverter<>(BigDecimal.class);

        testToAndFromString(onTest, BigDecimal.valueOf(02.0000230));
    }

    @Test(expected = NumberFormatException.class)
    public void bigDecimalInvalid() {
        CommonTypesStringConverter<BigDecimal> onTest = new CommonTypesStringConverter<>(BigDecimal.class);

        onTest.convertFrom("00.0.0");
    }

    @Test
    public void stringValid() {
        CommonTypesStringConverter<String> onTest = new CommonTypesStringConverter<>(String.class);

        testToAndFromString(onTest, "zazaza");
    }

    @Test
    public void timestampValid() {
        CommonTypesStringConverter<Timestamp> onTest = new CommonTypesStringConverter<>(Timestamp.class);

        testToAndFromString(onTest, new Timestamp(System.currentTimeMillis()));
    }

    @Test(expected = NumberFormatException.class)
    public void timestampInvalid() {
        CommonTypesStringConverter<Timestamp> onTest = new CommonTypesStringConverter<>(Timestamp.class);

        onTest.convertFrom("2012-04-01");
    }

}