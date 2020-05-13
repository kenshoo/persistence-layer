package com.kenshoo.pl.one2many;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.*;

public class OtherGrandChildEntity extends AbstractEntityType<OtherGrandChildEntity> {

    public static final OtherGrandChildEntity INSTANCE = new OtherGrandChildEntity();

    public static final EntityField<OtherGrandChildEntity, Integer> PARENT_ID = INSTANCE.field(OtherGrandChildTable.INSTANCE.parent_id);
    public static final EntityField<OtherGrandChildEntity, Integer> CHILD_ID = INSTANCE.field(OtherGrandChildTable.INSTANCE.child_id);
    public static final EntityField<OtherGrandChildEntity, String>  NAME = INSTANCE.field(OtherGrandChildTable.INSTANCE.name);

    private OtherGrandChildEntity() {
        super("testGrandChildEntity");
    }


    public static class ChildIdAndName extends PairUniqueKeyValue<OtherGrandChildEntity, Integer, String> {

        private static final PairUniqueKey<OtherGrandChildEntity, Integer, String> DEFINITION = new PairUniqueKey<OtherGrandChildEntity, Integer, String>(CHILD_ID, NAME) {
            @Override
            protected ChildIdAndName createValue(Integer childId, String name) {
                return new ChildIdAndName(childId, name);
            }
        };

        public ChildIdAndName(Integer childId, String name) {
            super(DEFINITION, childId, name);
        }
    }


    @Override
    public SupportedChangeOperation getSupportedOperation()  {
        return SupportedChangeOperation.CREATE_UPDATE_AND_DELETE;
    }

    @Override
    public DataTable getPrimaryTable() {
        return OtherGrandChildTable.INSTANCE;
    }
}
