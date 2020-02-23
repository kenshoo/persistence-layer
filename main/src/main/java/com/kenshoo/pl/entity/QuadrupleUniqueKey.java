package com.kenshoo.pl.entity;

public class QuadrupleUniqueKey<E extends EntityType<E>, A, B, C, D> extends UniqueKey<E> {

    private final EntityField<E, A> a;
    private final EntityField<E, B> b;
    private final EntityField<E, C> c;
    private final EntityField<E, D> d;

    public QuadrupleUniqueKey(EntityField<E, A> a, EntityField<E, B> b, EntityField<E, C> c, EntityField<E, D> d) {
        //noinspection unchecked
        super(new EntityField[]{a, b, c, d});
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    protected QuadrupleUniqueKeyValue<E, A, B, C, D> createValue(A a, B b, C c, D d) {
        return new QuadrupleUniqueKeyValue<>(new QuadrupleUniqueKey<>(this.a, this.b, this.c, this.d), a, b, c, d);
    }

    @Override
    public Identifier<E> createValue(FieldsValueMap<E> fieldsValueMap) {
        return createValue(fieldsValueMap.get(a), fieldsValueMap.get(b), fieldsValueMap.get(c), fieldsValueMap.get(d));
    }
}
