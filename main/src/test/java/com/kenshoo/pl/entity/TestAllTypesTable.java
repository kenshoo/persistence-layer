package com.kenshoo.pl.entity;

import com.kenshoo.jooq.AbstractDataTable;
import org.jooq.Record;
import org.jooq.TableField;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.jooq.impl.SQLDataType.*;

public class TestAllTypesTable extends AbstractDataTable<TestAllTypesTable> {

    public static final TestAllTypesTable TABLE = new TestAllTypesTable();

    private TestAllTypesTable() {
        super("testAllTypesTable");
    }

    public final TableField<Record, Integer> id = createPKField("id", INTEGER);
    public final TableField<Record, String> field_varchar = createField("field_varchar", VARCHAR(50));
    public final TableField<Record, Integer> field_integer = createField("field_integer", INTEGER);
    public final TableField<Record, Float> field_real = createField("field_real", REAL);
    public final TableField<Record, Double> field_double = createField("field_double", DOUBLE);
    public final TableField<Record, BigDecimal> field_decimal = createField("field_decimal", DECIMAL(6, 2));
    public final TableField<Record, LocalDate> field_localdate = createField("field_localdate", LOCALDATE);
    public final TableField<Record, LocalDateTime> field_localdatetime = createField("field_localdatetime", LOCALDATETIME);
    public final TableField<Record, Timestamp> field_timestamp = createField("field_timestamp", TIMESTAMP);

    @Override
    public TestAllTypesTable as(String alias) {
        return null;
    }
}
