package com.kenshoo.pl.auto.inc;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.annotation.Id;


public class ParentEntity extends AbstractEntityType<ParentEntity> {

    public static final ParentEntity INSTANCE = new ParentEntity();

    @Id
    public static final EntityField<ParentEntity, Integer> ID = INSTANCE.field(ParentTable.INSTANCE.id);

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

    public static class IdInTarget extends SingleUniqueKeyValue<ParentEntity, Integer> {
        public static final SingleUniqueKey<ParentEntity, Integer> DEFINITION = new SingleUniqueKey<>(ParentEntity.ID_IN_TARGET) {
            protected SingleUniqueKeyValue<ParentEntity, Integer> createValue(Integer value) {
                return new IdInTarget(value);
            }
        };

        public IdInTarget(Integer val) {
            super(DEFINITION, val);
        }
    }
}
