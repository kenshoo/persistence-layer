package com.kenshoo.pl.entity;

public class SextupleUniqueKey<E extends EntityType<E>, T1, T2, T3, T4, T5, T6> extends UniqueKey<E> {

    private final EntityField<E, T1> a;
    private final EntityField<E, T2> b;
    private final EntityField<E, T3> c;
    private final EntityField<E, T4> d;
    private final EntityField<E, T5> e;
    private final EntityField<E, T6> f;

    public SextupleUniqueKey(EntityField<E, T1> a,
                             EntityField<E, T2> b,
                             EntityField<E, T3> c,
                             EntityField<E, T4> d,
                             EntityField<E, T5> e,
                             EntityField<E, T6> f) {
        //noinspection unchecked
        super(new EntityField[]{a, b, c, d, e, f});
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
        this.f = f;
    }

    @Deprecated
    protected SextupleUniqueKeyValue<E, T1, T2, T3, T4, T5, T6> createValue(T1 a, T2 b, T3 c, T4 d, T5 e, T6 f) {
        return new SextupleUniqueKeyValue<>(this, a, b, c, d, e, f);
    }

    @Override
    public Identifier<E> createIdentifier(FieldsValueMap<E> fieldsValueMap) {
        return createValue(fieldsValueMap.get(a),
                           fieldsValueMap.get(b),
                           fieldsValueMap.get(c),
                           fieldsValueMap.get(d),
                           fieldsValueMap.get(e),
                           fieldsValueMap.get(f));
    }

    public Identifier<E> createIdentifier(T1 a, T2 b, T3 c, T4 d, T5 e, T6 f) {
        return createValue(a, b, c, d, e, f);
    }
}
