package com.kenshoo.pl.entity;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.data.CreateRecordCommand;
import com.kenshoo.pl.entity.converters.EnumAsStringValueConverter;

import static com.kenshoo.pl.data.CreateRecordCommand.OnDuplicateKey.IGNORE;

public class EntityForTestIgnoringOnCreate extends AbstractEntityType<EntityForTestIgnoringOnCreate> {

    public static final EntityForTestIgnoringOnCreate INSTANCE = new EntityForTestIgnoringOnCreate();

    public static final EntityField<EntityForTestIgnoringOnCreate, TestEnum> FIELD1 = INSTANCE.field(EntityForTestTable.INSTANCE.field1, EnumAsStringValueConverter.create(TestEnum.class));
    public static final EntityField<EntityForTestIgnoringOnCreate, Integer> ID = INSTANCE.field(EntityForTestTable.INSTANCE.id);
    public static final EntityField<EntityForTestIgnoringOnCreate, Integer> FIELD2 = INSTANCE.field(EntityForTestTable.INSTANCE.field2);
    public static final EntityField<EntityForTestIgnoringOnCreate, Integer> PARENT_ID = INSTANCE.field(EntityForTestTable.INSTANCE.parent_id);

    private EntityForTestIgnoringOnCreate() {
        super("test-ignore");
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
        return IGNORE;
    }

    public static class Key extends SingleUniqueKeyValue<EntityForTestIgnoringOnCreate, Integer> {
        public static final SingleUniqueKey<EntityForTestIgnoringOnCreate, Integer> DEFINITION = new SingleUniqueKey<EntityForTestIgnoringOnCreate, Integer>(EntityForTestIgnoringOnCreate.ID) {
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
