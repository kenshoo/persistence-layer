package com.kenshoo.pl.entity;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.annotation.DefaultValue;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.Immutable;
import com.kenshoo.pl.entity.annotation.Required;
import com.kenshoo.pl.entity.converters.EnumAsStringValueConverter;
import com.kenshoo.pl.entity.converters.TimestampValueConverter;
import org.jooq.Record;
import org.jooq.TableField;
import org.junit.Ignore;

import java.time.Instant;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.kenshoo.pl.entity.annotation.RequiredFieldType.RELATION;

@Ignore("This is not a test despite the name that ends with Test")
public class EntityForTest extends AbstractEntityType<EntityForTest> {

    public static final EntityForTest INSTANCE = new EntityForTest();

    @Id
    public static final EntityField<EntityForTest, Integer> ID = INSTANCE.field(EntityForTestTable.INSTANCE.id);
    public static final EntityField<EntityForTest, TestEnum> FIELD1 = INSTANCE.field(EntityForTestTable.INSTANCE.field1, EnumAsStringValueConverter.create(TestEnum.class));
    @DefaultValue("999")
    public static final EntityField<EntityForTest, Integer> FIELD2 = INSTANCE.field(EntityForTestTable.INSTANCE.field2);
    @CreationDate
    @Immutable
    public static final EntityField<EntityForTest, Instant> CREATION_DATE = INSTANCE.field(EntityForTestTable.INSTANCE.creationDate, TimestampValueConverter.INSTANCE);
    public static final EntityField<EntityForTest, String> COMPLEX_FIELD = INSTANCE.field(new ComplexFieldAdapter(), new CommonTypesStringConverter<>(String.class));
    public static final EntityField<EntityForTest, String> VIRTUAL_FIELD = INSTANCE.virtualField(FIELD1, FIELD2, (field1, field2) -> field1.name() + "-" + field2, new CommonTypesStringConverter<>(String.class), Objects::equals);
    @Immutable
    public static final EntityField<EntityForTest, String> IMMUTABLE_FIELD = INSTANCE.virtualField(VIRTUAL_FIELD, Function.identity(), new CommonTypesStringConverter<>(String.class), Objects::equals);
    public static final EntityField<EntityForTest, String> URL = INSTANCE.field(EntityForTestSecondaryTable.INSTANCE.url);
    public static final EntityField<EntityForTest, String> URL_PARAM = INSTANCE.field(EntityForTestSecondaryTable.INSTANCE.url_param);
    @Required(RELATION)
    public static final EntityField<EntityForTest, Integer> PARENT_ID = INSTANCE.field(EntityForTestTable.INSTANCE.parent_id);
    @IgnoredIfSetAlone
    public static final EntityField<EntityForTest, Integer> IGNORABLE_FIELD = INSTANCE.field(EntityForTestTable.INSTANCE.ignorableField);

    private EntityForTest() {
        super("test");
    }

    @Override
    public DataTable getPrimaryTable() {
        return EntityForTestTable.INSTANCE;
    }

    @Override
    public SupportedChangeOperation getSupportedOperation()  {
         return SupportedChangeOperation.CREATE_UPDATE_AND_DELETE;
    }

    public static class Key extends SingleUniqueKeyValue<EntityForTest, Integer> {
        public static final SingleUniqueKey<EntityForTest, Integer> DEFINITION = new SingleUniqueKey<EntityForTest, Integer>(EntityForTest.ID) {
            @Override
            protected Key createValue(Integer value) {
                return new Key(value);
            }
        };

        public Key(int id) {
            super(DEFINITION, id);
        }
    }

    public static class ComplexFieldAdapter implements EntityFieldDbAdapter<String> {

        private final TableField<Record, String> keyField = EntityForTestTable.INSTANCE.complexFieldKey;
        private final TableField<Record, String> valueField = EntityForTestTable.INSTANCE.complexFieldValue;

        @Override
        public DataTable getTable() {
            return EntityForTestTable.INSTANCE;
        }

        @Override
        public Stream<TableField<Record, ?>> getTableFields() {
            return Stream.of(keyField, valueField);
        }

        @Override
        public Stream<Object> getDbValues(String value) {
            return Stream.of((Object[]) value.split(":"));
        }

        @Override
        public String getFromRecord(Iterator<Object> valuesIterator) {
            return valuesIterator.next().toString() + ":" + valuesIterator.next().toString();
        }
    }

}
