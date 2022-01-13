package com.kenshoo.pl.one2many.events;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.annotation.Required;
import com.kenshoo.pl.entity.converters.EnumAsStringValueConverter;
import com.kenshoo.pl.one2many.relatedByNonPK.Type;

import static com.kenshoo.pl.entity.annotation.RequiredFieldType.RELATION;


public class ChildEntity extends AbstractEntityType<ChildEntity> {

    public static final ChildEntity INSTANCE = new ChildEntity();

    public static final EntityField<ChildEntity, Integer> ID = INSTANCE.field(ChildTable.INSTANCE.id);

    @Required(RELATION)
    public static final EntityField<ChildEntity, Integer> PARENT_ID = INSTANCE.field(ChildTable.INSTANCE.parent_id);

    @Required
    public static final EntityField<ChildEntity, String> CHILD_NAME = INSTANCE.field(ChildTable.INSTANCE.child_name);

    private ChildEntity() {
        super("child");
    }

    @Override
    public DataTable getPrimaryTable() {
        return ChildTable.INSTANCE;
    }

    @Override
    public SupportedChangeOperation getSupportedOperation()  {
         return SupportedChangeOperation.CREATE_UPDATE_AND_DELETE;
    }
}
