package com.kenshoo.pl.jooq;

import org.jooq.*;
import org.jooq.impl.DSL;

class TempTable<T extends Table<Record>> {

    private final DSLContext dslContext;
    private final T table;
    private final Field<?>[] fields;
    private final Type tableType;
    private final TablePopulator tablePopulator;

    public TempTable(DSLContext dslContext, T table, Field<?>[] fields, TablePopulator tablePopulator, Type tableType) {
        this.dslContext = dslContext;
        this.table = table;
        this.fields = fields;
        this.tableType = tableType;
        this.tablePopulator = tablePopulator;
    }

    public void create() {
        dropTable();
        createTable();
        populateTable();
    }

    public void dropTable() {
        DropTableFinalStep dropTable = dslContext.dropTableIfExists(table);
        dslContext.execute(dropTable.getSQL().replace("drop table", "drop temporary table"));
    }

    private void createTable() {
        CreateTableColumnStep createTableColumnStep = dslContext.createTemporaryTable(table).column(fields[0]);
        for (int i = 1; i < fields.length; i++) {
            Field field = fields[i];
            //noinspection unchecked
            createTableColumnStep = createTableColumnStep.column(field, field.getDataType());
        }
        CreateTableFinalStep createTableFinalStep = createTableColumnStep;
        UniqueKey<Record> primaryKey = table.getPrimaryKey();
        if (primaryKey != null) {
            createTableFinalStep = createTableColumnStep.constraint(DSL.constraint().primaryKey(primaryKey.getFields().toArray(new TableField[0])));
        }
        //noinspection ConstantConditions
        String tableCreateSql = createTableFinalStep.getSQL();

        if (tableType == Type.IN_MEMORY) {
            tableCreateSql = tableCreateSql.concat(" Engine=MEMORY");
        }
        dslContext.execute(tableCreateSql);
    }

    private void populateTable() {
        BatchBindStep batch = dslContext.batch(dslContext.insertInto(table, fields).values(new Object[fields.length]).onDuplicateKeyIgnore());
        tablePopulator.populate(batch);
        batch.execute();
    }

    enum Type {
        IN_MEMORY,
        REGULAR
    }

}
