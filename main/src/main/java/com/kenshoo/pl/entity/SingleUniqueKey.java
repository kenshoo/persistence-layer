package com.kenshoo.pl.entity;

/**
 *
 */
public class SingleUniqueKey<E extends EntityType<E>, A> extends UniqueKey<E> {

    private final EntityField<E, A> a;

    @SuppressWarnings("unchecked")
    public SingleUniqueKey(EntityField<E, A> a) {
        super(new EntityField[]{a});
        this.a = a;
    }

    @Deprecated
    protected SingleUniqueKeyValue<E, A> createValue(A value) {
        return new SingleUniqueKeyValue<>(this, value);
    }

    @Override
    public Identifier<E> createValue(FieldsValueMap<E> fieldsValueMap) {
        return createValue(fieldsValueMap.get(a));
    }

    public Identifier<E> createIdentifier(A value) {
        return createValue(value);
    }
}
