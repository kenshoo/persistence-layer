package com.kenshoo.pl.entity;

/**
 *
 */
public abstract class SingleUniqueKey<E extends EntityType<E>, A> extends UniqueKey<E> {

    private final EntityField<E, A> a;

    @SuppressWarnings("unchecked")
    public SingleUniqueKey(EntityField<E, A> a) {
        super(new EntityField[]{a});
        this.a = a;
    }

    protected abstract SingleUniqueKeyValue<E, A> createValue(A value);

    @Override
    public Identifier<E> createValue(FieldsValueMap<E> fieldsValueMap) {
        return createValue(fieldsValueMap.get(a));
    }
}
