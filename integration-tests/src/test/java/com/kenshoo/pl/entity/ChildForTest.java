package com.kenshoo.pl.entity;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.Required;
import org.junit.Ignore;

import static com.kenshoo.pl.entity.annotation.RequiredFieldType.RELATION;

@Ignore("This is not a test despite the name that ends with Test")
public class ChildForTest extends AbstractEntityType<ChildForTest> {

    public static final ChildForTest INSTANCE = new ChildForTest();

    @Id
    public static final EntityField<ChildForTest, Integer> ID = INSTANCE.field(ChildForTestTable.INSTANCE.id);
    @Required(RELATION)
    public static final EntityField<ChildForTest, Integer> PARENT_ID = INSTANCE.field(ChildForTestTable.INSTANCE.parent_id);
    public static final EntityField<ChildForTest, String> FIELD = INSTANCE.field(ChildForTestTable.INSTANCE.field);

    private ChildForTest() {
        super("test");
    }

    @Override
    public DataTable getPrimaryTable() {
        return ChildForTestTable.INSTANCE;
    }

    @Override
    public SupportedChangeOperation getSupportedOperation()  {
         return SupportedChangeOperation.CREATE_UPDATE_AND_DELETE;
    }

    public static class Key extends SingleUniqueKeyValue<ChildForTest, Integer> {
        public static final SingleUniqueKey<ChildForTest, Integer> DEFINITION = new SingleUniqueKey<ChildForTest, Integer>(ChildForTest.ID) {
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
