package com.kenshoo.pl.data;

import com.kenshoo.pl.jooq.DataTable;

public class CreateRecordCommand extends AbstractRecordCommand {

    public CreateRecordCommand(DataTable table) {
        super(table);
    }

    public enum OnDuplicateKey {
        FAIL,
        IGNORE,
        UPDATE
    }
}
