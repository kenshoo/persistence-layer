package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.Entity;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

public class PartialEntityInvocationHandler<E extends EntityType<E>> implements InvocationHandler {

    private final Map<Method, EntityField<E, ?>> methodsMap;
    private final Entity entity;

    public PartialEntityInvocationHandler(Map<Method, EntityField<E, ?>> methodsMap, Entity entity) {
        this.methodsMap = methodsMap;
        this.entity = entity;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        EntityField<E, ?> entityField = methodsMap.get(method);
        if (entityField != null) {
            return entity.get(entityField);
        }
        return method.invoke(entity, args);
    }
}
