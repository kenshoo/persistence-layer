package com.kenshoo.pl.one2many;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.*;

public class GreatGrandChildEntity extends AbstractEntityType<GreatGrandChildEntity> {

    public static final GreatGrandChildEntity INSTANCE = new GreatGrandChildEntity();

    public static final EntityField<GreatGrandChildEntity, Integer> PARENT_ID = INSTANCE.field(GreatGrandChildTable.INSTANCE.parent_id);
    public static final EntityField<GreatGrandChildEntity, String> GRANDCHILD_COLOR = INSTANCE.field(GreatGrandChildTable.INSTANCE.grandchild_color);
    public static final EntityField<GreatGrandChildEntity, String>  NAME = INSTANCE.field(GreatGrandChildTable.INSTANCE.name);

    private GreatGrandChildEntity() {
        super("otherGrandChildEntity");
    }


    public static class GrandchildColorAndName extends PairUniqueKeyValue<GreatGrandChildEntity, String, String> {

        private static final PairUniqueKey<GreatGrandChildEntity, String, String> DEFINITION = new PairUniqueKey<GreatGrandChildEntity, String, String>(GRANDCHILD_COLOR, NAME) {
            @Override
            protected GrandchildColorAndName createValue(String grandchildColor, String name) {
                return new GrandchildColorAndName(grandchildColor, name);
            }
        };

        public GrandchildColorAndName(String grandchildColor, String name) {
            super(DEFINITION, grandchildColor, name);
        }
    }


    @Override
    public SupportedChangeOperation getSupportedOperation()  {
        return SupportedChangeOperation.CREATE_UPDATE_AND_DELETE;
    }

    @Override
    public DataTable getPrimaryTable() {
        return GreatGrandChildTable.INSTANCE;
    }
}
