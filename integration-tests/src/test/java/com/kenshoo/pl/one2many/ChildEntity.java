package com.kenshoo.pl.one2many;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.*;


public class ChildEntity extends AbstractEntityType<ChildEntity> {

    public static final ChildEntity INSTANCE = new ChildEntity();

    public static final EntityField<ChildEntity, Integer> ID = INSTANCE.field(ChildTable.INSTANCE.id);

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

    public static class Id extends SingleUniqueKeyValue<ChildEntity, Integer> {

        public static final SingleUniqueKey<ChildEntity, Integer> DEFINITION =
                new SingleUniqueKey<ChildEntity, Integer>(ID) {
                    @Override
                    protected Id createValue(Integer Id) {
                        return new Id(Id);
                    }
                };

        public Id(int Id) {
            super(DEFINITION, Id);
        }
    }
}
