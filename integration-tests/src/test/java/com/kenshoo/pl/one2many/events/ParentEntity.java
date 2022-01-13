package com.kenshoo.pl.one2many.events;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.annotation.Required;

public class ParentEntity extends AbstractEntityType<ParentEntity> {

    public static final ParentEntity INSTANCE = new ParentEntity();

    public static final EntityField<ParentEntity, Integer> ID = INSTANCE.field(ParentTable.INSTANCE.id);

    @Required
    public static final EntityField<ParentEntity, String> ID_IN_TARGET = INSTANCE.field(ParentTable.INSTANCE.idInTarget);

    public static final EntityField<ParentEntity, String> NAME = INSTANCE.field(ParentTable.INSTANCE.name);

    public static final EntityField<ParentEntity, String> FIELD_TO_ENRICH = INSTANCE.field(ParentTable.INSTANCE.field_to_enrich);

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
}
