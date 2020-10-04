package com.kenshoo.pl.entity;

public class TripleUniqueKey<E extends EntityType<E>, A, B, C> extends UniqueKey<E> {

    private final EntityField<E, A> a;
    private final EntityField<E, B> b;
    private final EntityField<E, C> c;

    public TripleUniqueKey(EntityField<E, A> a, EntityField<E, B> b, EntityField<E, C> c) {
        //noinspection unchecked
        super(new EntityField[]{a, b, c});
        this.a = a;
        this.b = b;
        this.c = c;
    }

    @Deprecated
    protected TripleUniqueKeyValue<E, A, B, C> createValue(A a, B b, C c) {
        return new TripleUniqueKeyValue<>(this, a, b, c);
    }

    @Override
    public Identifier<E> createValue(FieldsValueMap<E> fieldsValueMap) {
        return createValue(fieldsValueMap.get(a), fieldsValueMap.get(b), fieldsValueMap.get(c));
    }

    public Identifier<E> createIdentifier(A a, B b, C c) {
        return createValue(a, b, c);
    }
}
