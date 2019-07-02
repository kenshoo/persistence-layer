package com.kenshoo.pl.entity.converters;

import com.kenshoo.pl.entity.ValueConverter;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class EnumAsStringValueConverterTest {

    @Test
    public void testConvertToDB() throws Exception {
        ValueConverter<TestEnum, String> syncStatusStringValueConverter = EnumAsStringValueConverter.create(TestEnum.class);
        assertEquals(TestEnum.AAA.name(), syncStatusStringValueConverter.convertTo(TestEnum.AAA));
    }

    @Test
    public void testConvertFromDBNull() throws Exception {
        ValueConverter<TestEnum, String> syncStatusStringValueConverter = EnumAsStringValueConverter.create(TestEnum.class);
        assertNull(syncStatusStringValueConverter.convertFrom(null));
    }

    @Test
    public void testConvertFromDBNotFound() throws Exception {
        ValueConverter<TestEnum, String> syncStatusStringValueConverter = EnumAsStringValueConverter.create(TestEnum.class);
        assertNull(syncStatusStringValueConverter.convertFrom("bla-bla"));
    }

    @Test
    public void testConvertFromDBFound() throws Exception {
        ValueConverter<TestEnum, String> syncStatusStringValueConverter = EnumAsStringValueConverter.create(TestEnum.class);
        assertEquals(TestEnum.BBB, syncStatusStringValueConverter.convertFrom(TestEnum.BBB.name()));
    }

    
    private static enum TestEnum {
        AAA, BBB, CCC
        
    }
}