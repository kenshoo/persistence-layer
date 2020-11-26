package com.kenshoo.pl.one2many.relatedByNonPK;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.annotation.Required;
import com.kenshoo.pl.entity.converters.EnumAsStringValueConverter;

import static com.kenshoo.pl.entity.annotation.RequiredFieldType.RELATION;


public class ChildEntity extends AbstractEntityType<ChildEntity> {

    public static final ChildEntity INSTANCE = new ChildEntity();

    public static final EntityField<ChildEntity, Integer> ID = INSTANCE.field(ChildTable.INSTANCE.id);

    @Required(RELATION)
    public static final EntityField<ChildEntity, Type> TYPE = INSTANCE.field(ChildTable.INSTANCE.type, EnumAsStringValueConverter.create(Type.class));

    @Required(RELATION)
    public static final EntityField<ChildEntity, Integer> ID_IN_TARGET = INSTANCE.field(ChildTable.INSTANCE.idInTarget);

    @Required
    public static final EntityField<ChildEntity, Integer> ORDINAL = INSTANCE.field(ChildTable.INSTANCE.ordinal);

    @Required
    public static final EntityField<ChildEntity, String> FIELD_1 = INSTANCE.field(ChildTable.INSTANCE.field1);

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

        public static final SingleUniqueKey<ChildEntity, Integer> DEFINITION = IdentifierType.uniqueKey(ORDINAL);

        public Ordinal(int ordinal) {
            super(DEFINITION,  ordinal);
        }
    }

    public static class Id extends SingleUniqueKeyValue<ChildEntity, Integer> {

        public static final SingleUniqueKey<ChildEntity, Integer> DEFINITION = IdentifierType.uniqueKey(ID);

        public Id(int Id) {
            super(DEFINITION, Id);
        }
    }
}
