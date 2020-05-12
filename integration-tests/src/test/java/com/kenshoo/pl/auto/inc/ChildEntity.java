package com.kenshoo.pl.auto.inc;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.*;


public class ChildEntity extends AbstractEntityType<ChildEntity> {

    public static final ChildEntity INSTANCE = new ChildEntity();


    public static final EntityField<ChildEntity, Integer> ORDINAL = INSTANCE.field(ChildTable.INSTANCE.ordinal);

    public static final EntityField<ChildEntity, String> FIELD_1 = INSTANCE.field(ChildTable.INSTANCE.field1);

    public static final EntityField<ChildEntity, Integer> PARENT_ID = INSTANCE.field(ChildTable.INSTANCE.parent_id);


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


    public static class Ordinal extends SingleUniqueKeyValue<ChildEntity, Integer> {

        public static final SingleUniqueKey<ChildEntity, Integer> DEFINITION =
                new SingleUniqueKey<ChildEntity, Integer>(ORDINAL) {
                    @Override
                    protected Ordinal createValue(Integer ordinal) {
                        return new Ordinal(ordinal);
                    }
                };

        public Ordinal(int ordinal) {
            super(DEFINITION, ordinal);
        }
    }

}
