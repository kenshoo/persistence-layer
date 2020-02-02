package com.kenshoo.pl.entity;

import org.apache.commons.lang3.ArrayUtils;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;


/**
 * Holds the value of the unique key
 * <p>
 * The order of values MUST match the order in the UniqueKey.
 * <p>
 * The instance is supposed to be immutable.
 * When mutable objects are added to values, they must not change after they were added.
 */
public class UniqueKeyValue<E extends EntityType<E>> implements Identifier<E> {

    private final UniqueKey<E> uniqueKey;
    protected final Object[] values;
    private int hashCode;

    public UniqueKeyValue(UniqueKey<E> uniqueKey, Object[] values) {
        this.uniqueKey = uniqueKey;
        this.values = values;
    }

    public static <E extends EntityType<E>> UniqueKeyValue<E> empty() {
        return new UniqueKeyValue<>(new UniqueKey<E>(new EntityField[0]), new Object[0]);
    }

    @Override
    public <T> boolean containsField(EntityField<E, T> field) {
        return Stream.of(uniqueKey.getFields()).filter(field::equals).findFirst().isPresent();
    }

    @Override
    public <T> T get(EntityField<E, T> field) {
        EntityField<E, ?>[] keyFields = uniqueKey.getFields();
        int index = 0;
        for (EntityField<E, ?> keyField : keyFields) {
            if (keyField.equals(field)) {
                //noinspection unchecked
                return (T) values[index];
            }
            index++;
        }

        throw new IllegalArgumentException("Field " + field + " is not a key field");
    }

    public UniqueKey<E> getUniqueKey() {
        return uniqueKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UniqueKeyValue)) return false;

        UniqueKeyValue that = (UniqueKeyValue) o;

        if (!uniqueKey.equals(that.uniqueKey)) return false;
        return Arrays.deepEquals(values, that.values);

    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            int result = uniqueKey.hashCode();
            result = 31 * result + Arrays.deepHashCode(values);
            hashCode = result;
        }
        return hashCode;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("{");
        EntityField<E, ?>[] fields = uniqueKey.getFields();
        String separator = "";
        int i = 0;
        for (EntityField<E, ?> field : fields) {
            stringBuilder.append(separator).append(field).append("=").append(values[i]);
            separator = ",";
            i++;
        }
        return stringBuilder.append("}").toString();
    }

    public Identifier<E> concat(Identifier<E> id2) {
        return concat(this, id2);
    }

    public static <E extends EntityType<E>> Identifier<E> concat(Identifier<E> id1, Identifier<E> id2) {
        if (id1 == null) {
            return id2;
        }
        if (id2 == null) {
            return id1;
        }

        Seq<Tuple2<EntityField<E, ?>, Object>> pairs1 = Seq.of(id1.getUniqueKey().getFields()).zip(id1.getValues());
        Seq<Tuple2<EntityField<E, ?>, Object>> pairs2 = Seq.of(id2.getUniqueKey().getFields()).zip(id2.getValues());
        Tuple2<Seq<EntityField<E, ?>>, Seq<Object>> merged = Seq.unzip(pairs1.concat(pairs2).distinct(pair -> pair.v1));

        return new UniqueKeyValue<>(new UniqueKey<>(merged.v1), merged.v2.toArray());
    }

}
