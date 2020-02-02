package com.kenshoo.pl.one2many;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.*;

public class GrandChildEntity extends AbstractEntityType<GrandChildEntity> {

    public static final GrandChildEntity INSTANCE = new GrandChildEntity();

    public static final EntityField<GrandChildEntity, Integer> CHILD_ID = INSTANCE.field(GrandChildTable.INSTANCE.child_id);
    public static final EntityField<GrandChildEntity, String>  COLOR = INSTANCE.field(GrandChildTable.INSTANCE.color);

    private GrandChildEntity() {
        super("testGrandChildEntity");
    }

    public static class Color extends SingleUniqueKeyValue<GrandChildEntity, String> {
        public static final SingleUniqueKey<GrandChildEntity, String> DEFINITION = new SingleUniqueKey<>(GrandChildEntity.COLOR) {
            @Override
            protected SingleUniqueKeyValue<GrandChildEntity, String> createValue(String value) {
                return new Color(value);
            }
        };

        public Color(String val) {
            super(DEFINITION, val);
        }
    }

    @Override
    public SupportedChangeOperation getSupportedOperation()  {
        return SupportedChangeOperation.CREATE_UPDATE_AND_DELETE;
    }


    @Override
    public DataTable getPrimaryTable() {
        return GrandChildTable.INSTANCE;
    }
}
