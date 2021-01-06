package com.kenshoo.pl.secondaryOfParent;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.converters.EnumAsStringValueConverter;
import com.kenshoo.pl.one2many.relatedByNonPK.Type;

public class MainEntity extends AbstractEntityType<MainEntity> {

    public static final MainEntity INSTANCE = new MainEntity();

    public static final EntityField<MainEntity, Integer> ID = INSTANCE.field(MainTable.INSTANCE.id);
    public static final EntityField<MainEntity, Integer> ID_IN_TARGET = INSTANCE.field(MainTable.INSTANCE.id_in_target);
    public static final EntityField<MainEntity, String> NAME = INSTANCE.field(MainTable.INSTANCE.name);
    public static final EntityField<MainEntity, Type> TYPE = INSTANCE.field(MainTable.INSTANCE.type, EnumAsStringValueConverter.create(Type.class));
    public static final EntityField<MainEntity, String> URL = INSTANCE.field(SecondaryTable2.INSTANCE.url);
    public static final EntityField<MainEntity, String> URL_PARAM = INSTANCE.field(SecondaryTable2.INSTANCE.url_param);
    public static final EntityField<MainEntity, Double> BUDGET = INSTANCE.field(SecondaryTable1.INSTANCE.budget);
    public static final EntityField<MainEntity, String> LOCATION = INSTANCE.field(SecondaryTable3.INSTANCE.location);

    private MainEntity() {
        super("main");
    }

    @Override
    public DataTable getPrimaryTable() {
        return MainTable.INSTANCE;
    }

    @Override
    public SupportedChangeOperation getSupportedOperation()  {
         return SupportedChangeOperation.CREATE_UPDATE_AND_DELETE;
    }

    public static class Key extends SingleUniqueKeyValue<MainEntity, Integer> {
        public static final SingleUniqueKey<MainEntity, Integer> DEFINITION = new SingleUniqueKey<MainEntity, Integer>(MainEntity.ID) {
            @Override
            protected Key createValue(Integer value) {
                return new Key(value);
            }
        };

        public Key(int id) {
            super(DEFINITION, id);
        }
    }

}
