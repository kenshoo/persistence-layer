package com.kenshoo.pl.entity;

import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by Yuval on 05/07/2016.
 */
public class CommonTypesStringConverterTest {

    @Test(expected = IllegalArgumentException.class)
    public void unsupportedClass() {
        new CommonTypesStringConverter<>(CurrentEntityState.class);
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
    public void bigIntegerValid() {
        CommonTypesStringConverter<BigInteger> onTest = new CommonTypesStringConverter<>(BigInteger.class);

        testToAndFromString(onTest, new BigInteger("12345678901234567890"));
    }

    @Test(expected = NumberFormatException.class)
    public void bigIntegerWithDecimalPoint() {
        CommonTypesStringConverter<BigInteger> onTest = new CommonTypesStringConverter<>(BigInteger.class);

        onTest.convertFrom("12.34");
    }

    @Test(expected = NumberFormatException.class)
    public void bigIntegerWithLetters() {
        CommonTypesStringConverter<BigInteger> onTest = new CommonTypesStringConverter<>(BigInteger.class);

        onTest.convertFrom("12AB");
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

    @Test
    public void localDateValid() {
        CommonTypesStringConverter<LocalDate> onTest = new CommonTypesStringConverter<>(LocalDate.class);

        testToAndFromString(onTest, LocalDate.of(2020, 3, 31));
    }

    @Test(expected = DateTimeParseException.class)
    public void localDateInvalidCase1() {
        CommonTypesStringConverter<LocalDate> onTest = new CommonTypesStringConverter<>(LocalDate.class);

        onTest.convertFrom("1234");
    }

    @Test(expected = DateTimeParseException.class)
    public void localDateInvalidCase2() {
        CommonTypesStringConverter<LocalDate> onTest = new CommonTypesStringConverter<>(LocalDate.class);

        onTest.convertFrom("2020/01/01");
    }

    @Test(expected = DateTimeParseException.class)
    public void localDateInvalidCase3() {
        CommonTypesStringConverter<LocalDate> onTest = new CommonTypesStringConverter<>(LocalDate.class);

        onTest.convertFrom("2020-01");
    }
}