package com.kenshoo.pl.entity;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class InverseValueConverterTest {

    private final ValueConverter<String, TestEnum> stringToEnumConverter = new ValueConverter<>() {
        @Override
        public TestEnum convertTo(String value) {
            return TestEnum.valueOf(value);
        }

        @Override
        public String convertFrom(TestEnum value) {
            return value.name();
        }

        @Override
        public Class<String> getValueClass() {
            return String.class;
        }
    };

    private final InverseValueConverter<TestEnum, String> onTest = new InverseValueConverter<>(stringToEnumConverter, TestEnum.class);

    @Test
    public void testConvertFrom(){
        assertThat(onTest.convertFrom("AAA"), is(TestEnum.AAA));
    }

    @Test
    public void testConvertTo(){
        assertThat(onTest.convertTo(TestEnum.AAA), is("AAA"));
    }

    @Test
    public void testValueClass(){
        assertThat(onTest.getValueClass().getSimpleName(), is("TestEnum"));
    }

    private enum TestEnum {
        AAA
    }

}