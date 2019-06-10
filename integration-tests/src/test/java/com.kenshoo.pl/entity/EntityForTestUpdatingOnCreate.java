package com.kenshoo.pl.entity;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.data.CreateRecordCommand;
import com.kenshoo.pl.entity.converters.EnumAsStringValueConverter;

import static com.kenshoo.pl.data.CreateRecordCommand.OnDuplicateKey.UPDATE;

/**
 * Created by tzachz on 4/20/18
 */
public class EntityForTestUpdatingOnCreate extends AbstractEntityType<EntityForTestUpdatingOnCreate> {

    public static final EntityForTestUpdatingOnCreate INSTANCE = new EntityForTestUpdatingOnCreate();

    public static final EntityField<EntityForTestUpdatingOnCreate, TestEnum> FIELD1 = INSTANCE.field(EntityForTestTable.INSTANCE.field1, EnumAsStringValueConverter.create(TestEnum.class));
    public static final EntityField<EntityForTestUpdatingOnCreate, Integer> ID = INSTANCE.field(EntityForTestTable.INSTANCE.id);
    public static final EntityField<EntityForTestUpdatingOnCreate, Integer> FIELD2 = INSTANCE.field(EntityForTestTable.INSTANCE.field2);
    public static final EntityField<EntityForTestUpdatingOnCreate, Integer> PARENT_ID = INSTANCE.field(EntityForTestTable.INSTANCE.parent_id);

    private EntityForTestUpdatingOnCreate() {
        super("test-update");
    }

    @Override
    public DataTable getPrimaryTable() {
        return EntityForTestTable.INSTANCE;
    }

    @Override
    public SupportedChangeOperation getSupportedOperation()  {
        return SupportedChangeOperation.CREATE_UPDATE_AND_DELETE;
    }

    @Override
    public CreateRecordCommand.OnDuplicateKey onDuplicateKey() {
        return UPDATE;
    }

    public static class Key extends SingleUniqueKeyValue<EntityForTestUpdatingOnCreate, Integer> {
        public static final SingleUniqueKey<EntityForTestUpdatingOnCreate, Integer> DEFINITION = new SingleUniqueKey<EntityForTestUpdatingOnCreate, Integer>(EntityForTestUpdatingOnCreate.ID) {
            @Override
            protected EntityForTestUpdatingOnCreate.Key createValue(Integer value) {
                return new EntityForTestUpdatingOnCreate.Key(value);
            }
        };

        public Key(int id) {
            super(DEFINITION, id);
        }
    }
}