package com.kenshoo.pl.entity;

public class PairUniqueKey<E extends EntityType<E>, A, B> extends UniqueKey<E> {
    private final EntityField<E, A> a;
    private final EntityField<E, B> b;

    public PairUniqueKey(EntityField<E, A> a, EntityField<E, B> b) {
        //noinspection unchecked
        super(new EntityField[]{a, b});
        this.a = a;
        this.b = b;
    }

    @Deprecated
    protected PairUniqueKeyValue<E, A, B> createValue(A a, B b) {
        return new PairUniqueKeyValue<>(this, a, b);
    }

    @Override
    public Identifier<E> createIdentifier(FieldsValueMap<E> fieldsValueMap) {
        return createValue(fieldsValueMap.get(a), fieldsValueMap.get(b));
    }

    public Identifier<E> createIdentifier(A a, B b) {
        return createValue(a, b);
    }
}
