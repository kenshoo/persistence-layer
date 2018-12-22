package com.kenshoo.pl.entity.internal;

import com.google.common.base.Throwables;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.kenshoo.pl.entity.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

public abstract class EntityTypeReflectionUtil {

    static public <E extends EntityType<E>, T> Set<EntityField<E, T>> getFieldsByPrototype(E entityType, EntityFieldPrototype<T> entityFieldPrototype) {
        //noinspection unchecked
        return StreamSupport.stream(entityType.getPrototypedFields().spliterator(), false)
                .filter(prototypedEntityField -> prototypedEntityField.getEntityFieldPrototype() == entityFieldPrototype)
                .map(prototypedEntityField -> (EntityField<E, T>) prototypedEntityField)
                .collect(toSet());
    }

    static public <E extends EntityType<E>> Map<EntityFieldPrototype<?>, EntityField<E, ?>> getFieldMappingByPrototype(E entityType, Collection<EntityFieldPrototype<?>> entityFieldPrototypes) {
        return StreamSupport.stream(entityType.getPrototypedFields().spliterator(), false)
                .filter(prototypedEntityField -> entityFieldPrototypes.contains(prototypedEntityField.getEntityFieldPrototype()))
                .collect(toMap(PrototypedEntityField::getEntityFieldPrototype, identity()));
    }

    static public <E extends EntityType<E>, FE extends PartialEntity> Map<Method, EntityField<E, ?>> getMethodsMap(E entityType, Class<FE> fetchedEntityIface) {
        if (!fetchedEntityIface.isInterface()) {
            throw new IllegalArgumentException("Only interfaces are supported, " + fetchedEntityIface.getName() + " isn't");
        }
        return Arrays.asList(fetchedEntityIface.getMethods()).stream()
                .filter(EntityTypeReflectionUtil::methodNameStartsWithGet)
                .collect(Collectors.toMap(Function.identity(), method -> toEntityField(entityType, method)));
    }

    private static boolean methodNameStartsWithGet(Method method) {
        String methodName = method.getName();
        if (!methodName.startsWith("get")) {
            throw new IllegalArgumentException("Only methods starting with \"get\" are supported, " + methodName + " doesn't");
        }
        return true;
    }

    private static String toEntityFieldName(String methodName) {
        String camelCase = methodName.substring("get".length());
        StringBuilder fieldIdentifier = new StringBuilder();
        for (char character : camelCase.toCharArray()) {
            if (Character.isUpperCase(character) && fieldIdentifier.length() > 0) {
                fieldIdentifier.append("_");
            }
            fieldIdentifier.append(Character.toUpperCase(character));
        }
        return fieldIdentifier.toString();
    }

    private static <E extends EntityType<E>> EntityField<E, ?> toEntityField(E entityType, Method method) {
        String methodName = method.getName();
        String name = toEntityFieldName(methodName);
        try {
            Field declaredField = entityType.getClass().getDeclaredField(name);
            //noinspection unchecked
            return (EntityField<E, ?>) declaredField.get(null);
        } catch (NoSuchFieldException | ClassCastException e) {
            throw new IllegalArgumentException("No entity field corresponds to method " + methodName);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public static <E extends EntityType<E>> BiMap<String, EntityField<E, ?>> getFieldToNameBiMap(Class<? extends AbstractEntityType> entityClass) {
        Map<String, EntityField<E, ?>> map = Stream.of(entityClass.getFields())
                .filter(field -> EntityField.class.isAssignableFrom(field.getType()))
                .collect(Collectors.toMap(
                                Field::getName,
                                EntityTypeReflectionUtil::getEntityFieldInstance
                        )
                );

        return HashBiMap.create(map);
    }

    private static <E extends EntityType<E>, T> EntityField<E, T> getEntityFieldInstance(Field field) {
        try {
            //noinspection unchecked
            return (EntityField<E, T>) field.get(null);
        } catch (IllegalAccessException e) {
            // Shouldn't happen
            throw Throwables.propagate(e);
        }
    }

    public static <E extends EntityType<E>, A extends Annotation> A getFieldAnnotation(EntityType<E> entityType, EntityField<E, ?> entityField, Class<A> annotationType) {
        try {
            Field field = entityType.getClass().getField(entityType.toFieldName(entityField));
            return field.getAnnotation(annotationType);
        } catch (NoSuchFieldException e) {
            // Shouldn't happen
            throw Throwables.propagate(e);
        }
    }

}
