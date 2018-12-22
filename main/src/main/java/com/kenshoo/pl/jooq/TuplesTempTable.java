package com.kenshoo.pl.jooq;

import org.jooq.DataType;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.AbstractKeys;
import org.jooq.impl.TableImpl;

import java.util.ArrayList;
import java.util.Collection;

public class TuplesTempTable extends TableImpl<Record> {

    public final Collection<TableField<Record, ?>> fields = new ArrayList<>();

    public TuplesTempTable() {
        super("tmp_ids");
    }

    public <T> TableField<Record, T> addField(String name, DataType<T> dataType) {
        TableField<Record, T> field = super.createField(name, dataType);
        fields.add(field);
        return field;
    }

    @Override
    public UniqueKey<Record> getPrimaryKey() {
        return new PrimaryKey().getPK();
    }

    private class PrimaryKey extends AbstractKeys {
        @SuppressWarnings("unchecked")
        UniqueKey<Record> getPK() {
            return createUniqueKey(TuplesTempTable.this, fields.toArray(new TableField[fields.size()]));
        }
    }
}
