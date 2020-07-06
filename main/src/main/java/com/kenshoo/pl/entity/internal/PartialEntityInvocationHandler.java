package com.kenshoo.pl.entity.internal;

import com.kenshoo.pl.entity.Entity;
import com.kenshoo.pl.entity.EntityField;
import com.kenshoo.pl.entity.EntityType;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

public class PartialEntityInvocationHandler<E extends EntityType<E>> implements InvocationHandler {

    private final Map<Method, EntityField<E, ?>> methodsMap;
    private final Entity currentState;

    public PartialEntityInvocationHandler(Map<Method, EntityField<E, ?>> methodsMap, Entity currentState) {
        this.methodsMap = methodsMap;
        this.currentState = currentState;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        EntityField<E, ?> entityField = methodsMap.get(method);
        if (entityField != null) {
            return  currentState.get(entityField);
        }
        return method.invoke(currentState, args);
    }
}
