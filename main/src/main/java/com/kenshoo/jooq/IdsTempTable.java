package com.kenshoo.jooq;

import org.jooq.DataType;
import org.jooq.Record;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.AbstractKeys;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

public class IdsTempTable<T> extends TableImpl<Record> {
    public static final IdsTempTable<Integer> INT_TABLE = new IdsTempTable<>(SQLDataType.INTEGER);
    public static final IdsTempTable<Long> LONG_TABLE = new IdsTempTable<>(SQLDataType.BIGINT);
    public static final IdsTempTable<String> STRING_SIZE_255_TABLE = new IdsTempTable<>(SQLDataType.VARCHAR.length(255));
    public static final IdsTempTable<String> STRING_SIZE_250_TABLE = new IdsTempTable<>(SQLDataType.VARCHAR.length(250));
    public static final IdsTempTable<String> STRING_SIZE_200_TABLE = new IdsTempTable<>(SQLDataType.VARCHAR.length(200));
    public static final IdsTempTable<String> STRING_SIZE_150_TABLE = new IdsTempTable<>(SQLDataType.VARCHAR.length(150));
    public static final IdsTempTable<String> STRING_IZE_100_TABLE = new IdsTempTable<>(SQLDataType.VARCHAR.length(100));
    public static final IdsTempTable<String> STRING_SIZE_50_TABLE = new IdsTempTable<>(SQLDataType.VARCHAR.length(50));
    public static final IdsTempTable<String> STRING_SIZE_40_TABLE = new IdsTempTable<>(SQLDataType.VARCHAR.length(40));
    public static final IdsTempTable<String> STRING_SIZE_30_TABLE = new IdsTempTable<>(SQLDataType.VARCHAR.length(30));
    public static final IdsTempTable<String> STRING_SIZE_20_TABLE = new IdsTempTable<>(SQLDataType.VARCHAR.length(20));
    public static final IdsTempTable<String> STRING_SIZE_10_TABLE = new IdsTempTable<>(SQLDataType.VARCHAR.length(10));

    public final TableField<Record, T> id;

    public IdsTempTable(DataType<T> idType) {
        super("tmp_ids");
        id = createField("id", idType, this);
    }

    public IdsTempTable(String alias, IdsTempTable<T> aliased) {
        super(alias, null, aliased);
        id = createField("id", aliased.id.getDataType(), this);
    }

    public IdsTempTable<T> as(String alias) {
        return new IdsTempTable<>(alias, this);
    }

    @Override
    public UniqueKey<Record> getPrimaryKey() {
        return new PrimaryKey().getPK();
    }

    private class PrimaryKey extends AbstractKeys {
        @SuppressWarnings("unchecked")
        UniqueKey<Record> getPK() {
            return createUniqueKey(IdsTempTable.this, id);
        }
    }
}
