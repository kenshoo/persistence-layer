package com.kenshoo.pl.entity;

/**
 *
 */
public class QuintupleUniqueKey<E extends EntityType<E>, T1, T2, T3, T4, T5> extends UniqueKey<E> {

    private final EntityField<E, T1> a;
    private final EntityField<E, T2> b;
    private final EntityField<E, T3> c;
    private final EntityField<E, T4> d;
    private final EntityField<E, T5> e;

    public QuintupleUniqueKey(EntityField<E, T1> a, EntityField<E, T2> b, EntityField<E, T3> c, EntityField<E, T4> d, EntityField<E, T5> e) {
        //noinspection unchecked
        super(new EntityField[]{a, b, c, d, e});
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
    }

    @Deprecated
    protected QuintupleUniqueKeyValue<E, T1, T2, T3, T4, T5> createValue(T1 a, T2 b, T3 c, T4 d, T5 e) {
        return new QuintupleUniqueKeyValue<>(this, a, b, c, d,e );
    }

    @Override
    public Identifier<E> createValue(FieldsValueMap<E> fieldsValueMap) {
        return createValue(fieldsValueMap.get(a), fieldsValueMap.get(b), fieldsValueMap.get(c), fieldsValueMap.get(d), fieldsValueMap.get(e));
    }

    public Identifier<E> createIdentifier(T1 a, T2 b, T3 c, T4 d, T5 e) {
        return createValue(a, b, c, d, e);
    }
}
