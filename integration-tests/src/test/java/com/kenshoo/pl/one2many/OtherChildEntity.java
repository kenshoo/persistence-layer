package com.kenshoo.pl.one2many;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.AbstractEntityType;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.SupportedChangeOperation;


public class OtherChildEntity extends AbstractEntityType<OtherChildEntity> {

    public static final OtherChildEntity INSTANCE = new OtherChildEntity();

    public static final EntityField<OtherChildEntity, Integer> ID = INSTANCE.field(OtherChildTable.INSTANCE.id);

    public static final EntityField<OtherChildEntity, String> NAME = INSTANCE.field(OtherChildTable.INSTANCE.name);

    public static final EntityField<OtherChildEntity, Integer> PARENT_ID = INSTANCE.field(OtherChildTable.INSTANCE.parent_id);

    private OtherChildEntity() {
        super("otherChild");
    }

    @Override
    public DataTable getPrimaryTable() {
        return OtherChildTable.INSTANCE;
    }

    @Override
    public SupportedChangeOperation getSupportedOperation()  {
         return SupportedChangeOperation.CREATE_UPDATE_AND_DELETE;
    }
}
