package com.kenshoo.pl.jooq;

import org.jooq.ForeignKey;
import org.jooq.Record;
import org.jooq.Table;

import java.util.Collection;
import java.util.List;

public interface DataTable extends Table<Record> {

    Collection<FieldAndValue<?>> getVirtualPartition();

    default ForeignKey<Record, Record> getForeignKey(DataTable primaryTable) {
        List<ForeignKey<Record, Record>> foreignKeys = getReferencesTo(primaryTable);
        if (foreignKeys.size() == 0) {
            throw new IllegalStateException("Table " + getName() + " does not define a foreign key to table " + primaryTable.getName());
        }
        if (foreignKeys.size() > 1) {
            throw new IllegalStateException("Table " + getName() + " defines several foreign keys to table " + primaryTable.getName());
        }

        return foreignKeys.get(0);
    }

}
