package com.kenshoo.pl.entity;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.annotation.Id;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Optional;

import static com.kenshoo.pl.entity.TestAllTypesTable.TABLE;

public class TestEntityWithIrreversibleConverters extends AbstractEntityType<TestEntityWithIrreversibleConverters> {

    public static final TestEntityWithIrreversibleConverters INSTANCE = new TestEntityWithIrreversibleConverters();

    @Id
    public static final EntityField<TestEntityWithIrreversibleConverters, Integer> ID = INSTANCE.field(TABLE.id);
    public static final EntityField<TestEntityWithIrreversibleConverters, String> FIELD_VARCHAR =
            INSTANCE.field(TABLE.field_varchar, new OneWayTrimConverter());
    public static final EntityField<TestEntityWithIrreversibleConverters, BigDecimal> FIELD_INTEGER =
            INSTANCE.field(TABLE.field_integer, new BigDecimalToIntegerConverter());
    public static final EntityField<TestEntityWithIrreversibleConverters, BigDecimal> FIELD_DOUBLE =
            INSTANCE.field(TABLE.field_double, new BigDecimalToDoubleConverter());

    private TestEntityWithIrreversibleConverters() {
        super("testIrreversibleConverters");
    }


    @Override
    public DataTable getPrimaryTable() {
        return TABLE;
    }

    private static class OneWayTrimConverter implements ValueConverter<String, String> {
        @Override
        public String convertTo(String value) {
            return Optional.of(value)
                    .map(String::trim)
                    .orElse(null);
        }

        @Override
        public String convertFrom(String value) {
            return value;
        }

        @Override
        public Class<String> getValueClass() {
            return String.class;
        }
    }

    private static class BigDecimalToIntegerConverter implements ValueConverter<BigDecimal, Integer> {

        @Override
        public Integer convertTo(final BigDecimal bigDecimal) {
            return Optional.of(bigDecimal)
                    .map(BigDecimal::intValue)
                    .orElse(null);
        }

        @Override
        public BigDecimal convertFrom(final Integer integer) {
            return Optional.of(integer)
                    .map(BigDecimal::new)
                    .orElse(null);
        }

        @Override
        public Class<BigDecimal> getValueClass() {
            return BigDecimal.class;
        }
    }

    private static class BigDecimalToDoubleConverter implements ValueConverter<BigDecimal, Double> {

        @Override
        public Double convertTo(final BigDecimal bigDecimal) {
            return Optional.of(bigDecimal)
                    .map(BigDecimal::doubleValue)
                    .orElse(null);
        }

        @Override
        public BigDecimal convertFrom(final Double aDouble) {
            return Optional.of(aDouble)
                    .map(d -> new BigDecimal(d, new MathContext(5)))
                    .orElse(null);
        }

        @Override
        public Class<BigDecimal> getValueClass() {
            return BigDecimal.class;
        }
    }
}
