package com.kenshoo.pl.auto.inc;

import com.kenshoo.jooq.DataTable;
import com.kenshoo.pl.entity.*;
import com.kenshoo.pl.entity.annotation.Id;
import com.kenshoo.pl.entity.annotation.Required;

import static com.kenshoo.pl.entity.annotation.RequiredFieldType.RELATION;


public class ParentEntityWithRequiredRelation extends AbstractEntityType<ParentEntityWithRequiredRelation> {

    public static final ParentEntityWithRequiredRelation INSTANCE = new ParentEntityWithRequiredRelation();

    @Id
    public static final EntityField<ParentEntityWithRequiredRelation, Integer> ID = INSTANCE.field(ParentTable.INSTANCE.id);

    public static final EntityField<ParentEntityWithRequiredRelation, Integer> ID_IN_TARGET = INSTANCE.field(ParentTable.INSTANCE.idInTarget);

    @Required(RELATION)
    public static final EntityField<ParentEntityWithRequiredRelation, Integer> GRAND_PARENT_ID = INSTANCE.field(ParentTable.INSTANCE.grand_parent_id);

    private ParentEntityWithRequiredRelation() {
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

    public static class Key extends SingleUniqueKeyValue<ParentEntityWithRequiredRelation, Integer> {
        public static final SingleUniqueKey<ParentEntityWithRequiredRelation, Integer> DEFINITION = new SingleUniqueKey<ParentEntityWithRequiredRelation, Integer>(ParentEntityWithRequiredRelation.ID) {
            @Override
            protected Key createValue(Integer value) {
                return new Key(value);
            }
        };

        public Key(int id) {
            super(DEFINITION, id);
        }
    }

    public static class UniqueKey extends SingleUniqueKeyValue<ParentEntityWithRequiredRelation, Integer> {
        public static final SingleUniqueKey<ParentEntityWithRequiredRelation, Integer> DEFINITION = new SingleUniqueKey<ParentEntityWithRequiredRelation, Integer>(ParentEntityWithRequiredRelation.ID_IN_TARGET) {
            @Override
            protected UniqueKey createValue(Integer value) {
                return new UniqueKey(value);
            }
        };

        public UniqueKey(Integer val) {
            super(DEFINITION, val);
        }
    }
}
