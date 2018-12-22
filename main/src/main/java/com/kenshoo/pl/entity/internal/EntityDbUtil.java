package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.data.DatabaseId;
import com.kenshoo.pl.entity.*;
import org.jooq.Record;
import org.jooq.TableField;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;


public class EntityDbUtil {

    private EntityDbUtil() {
    }

    public static <E extends EntityType<E>> DatabaseId getDatabaseId(Identifier<E> identifier) {
        EntityField<E, ?>[] fields = identifier.getUniqueKey().getFields();
        List<Object> dbValues = new ArrayList<>();
        List<TableField<Record, ?>> tableFields = new ArrayList<>();
        for (EntityField<E, ?> field : fields) {
            addToArrays(identifier, field, dbValues, tableFields);
        }
        return new DatabaseId(tableFields.toArray(new TableField[tableFields.size()]), dbValues.toArray(new Object[dbValues.size()]));
    }

    private static <E extends EntityType<E>, T> void addToArrays(Identifier<E> identifier, EntityField<E, T> field, List<Object> dbValues, List<TableField<Record, ?>> tableFields) {
        EntityFieldDbAdapter<T> dbAdapter = field.getDbAdapter();
        //noinspection unchecked
        dbAdapter.getTableFields().sequential().forEach(tableFields::add);
        //noinspection unchecked
        dbAdapter.getDbValues(identifier.get(field)).sequential().forEach(dbValues::add);
    }

    public static <E extends EntityType<E>> Object[] getFieldValues(Collection<EntityField<E, ?>> fields, FieldsValueMap<E> fieldsValueMap) {
        return getFieldValuesInner(fields, field -> getDbValues(fieldsValueMap, field, FieldsValueMap::get));
    }

    public static <E extends EntityType<E>> Object[] getFieldValues(Collection<EntityField<E, ?>> fields, Entity entity) {
        return getFieldValuesInner(fields, field -> getDbValues(entity, field, Entity::get));
    }

    private static <E extends EntityType<E>> Object[] getFieldValuesInner(Collection<EntityField<E, ?>> fields, Function<EntityField<E, ?>, Stream<?>> mapper) {
        return fields.stream().flatMap(mapper).toArray();
    }

    private static <E extends EntityType<E>, O, T> Stream<Object> getDbValues(O entity, EntityField<E, T> field, BiFunction<O, EntityField<E, T>, T> getter) {
        return field.getDbAdapter().getDbValues(getter.apply(entity, field));
    }

}
