package com.kenshoo.pl.one2many.relatedByNonPK;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.annotation.Required;
import com.kenshoo.pl.entity.converters.EnumAsStringValueConverter;


public class ParentEntity extends AbstractEntityType<ParentEntity> {

    public static final ParentEntity INSTANCE = new ParentEntity();

    public static final EntityField<ParentEntity, Integer> ID = INSTANCE.field(ParentTable.INSTANCE.id);

    @Required
    public static final EntityField<ParentEntity, Type> TYPE = INSTANCE.field(ParentTable.INSTANCE.type, EnumAsStringValueConverter.create(Type.class));

    @Required
    public static final EntityField<ParentEntity, Integer> ID_IN_TARGET = INSTANCE.field(ParentTable.INSTANCE.idInTarget);

    public static final EntityField<ParentEntity, String> NAME = INSTANCE.field(ParentTable.INSTANCE.name);

    private ParentEntity() {
        super("parent");
    }

    @Override
    public DataTable getPrimaryTable() {
        return ParentTable.INSTANCE;
    }

    @Override
    public SupportedChangeOperation getSupportedOperation()  {
        return SupportedChangeOperation.CREATE_UPDATE_AND_DELETE;
    }

    public static class Key extends SingleUniqueKeyValue<ParentEntity, Integer> {
        public static final SingleUniqueKey<ParentEntity, Integer> DEFINITION = IdentifierType.uniqueKey(ID);

        public Key(int id) {
            super(DEFINITION, id);
        }
    }

    public static class UniqueKey extends PairUniqueKeyValue<ParentEntity, Type, Integer> {
        public static final PairUniqueKey<ParentEntity, Type, Integer> DEFINITION =  IdentifierType.uniqueKey(TYPE, ID_IN_TARGET);

        public UniqueKey(Type type, Integer idInTarget) {
            super(DEFINITION, type, idInTarget);
        }
    }
}
