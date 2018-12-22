package com.kenshoo.pl.entity;

/**
 *
 */
public abstract class PairUniqueKey<E extends EntityType<E>, A, B> extends UniqueKey<E> {
    private final EntityField<E, A> a;
    private final EntityField<E, B> b;

    public PairUniqueKey(EntityField<E, A> a, EntityField<E, B> b) {
        //noinspection unchecked
        super(new EntityField[]{a, b});
        this.a = a;
        this.b = b;
    }

    protected abstract PairUniqueKeyValue<E, A, B> createValue(A a, B b);

    @Override
    public Identifier<E> createValue(FieldsValueMap<E> fieldsValueMap) {
        return createValue(fieldsValueMap.get(a), fieldsValueMap.get(b));
    }
}
