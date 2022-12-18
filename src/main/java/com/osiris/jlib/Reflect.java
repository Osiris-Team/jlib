package com.osiris.jlib;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

public class Reflect {
    public static <T> T newInstance(Class<T> type, Object... paramValues) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        // CLASS IS NOT ENUM
        // Create an instance/object of the provided type, which then later gets returned:
        T instance;
        if (type.getDeclaredConstructors().length == 0) {
            instance = type.newInstance();
        } else {
            Constructor<?> constructor = getTopConstructor(type);
            Parameter[] params = constructor.getParameters();
            if (params.length > 0) {
                Object[] actualParamValues = new Object[params.length];
                int iStart = 0;
                if(paramValues != null){
                    iStart = paramValues.length;
                    for (int i = 0; i < paramValues.length; i++) {
                        actualParamValues[i] = paramValues[i];
                    }
                }
                for (int i = iStart; i < params.length; i++) {
                    actualParamValues[i] = 0;
                    if (isPrimitive(params[i].getType()))
                        actualParamValues[i] = 0;
                    else
                        actualParamValues[i] = null;
                }
                if (!Modifier.isPublic(constructor.getModifiers())) constructor.setAccessible(true);
                instance = (T) constructor.newInstance(actualParamValues);
            } else {
                if (!Modifier.isPublic(constructor.getModifiers())) constructor.setAccessible(true);
                instance = (T) constructor.newInstance();
            }
        }
        return instance;
    }

    /**
     * Returns the declared constructor with the least parameters.
     */
    public static Constructor getTopConstructor(Class<?> clazz) {
        Constructor<?> result = null;
        for (Constructor<?> c : clazz.getDeclaredConstructors()) {
            if (result == null) result = c;
            else if (c.getParameterCount() < result.getParameterCount()) result = c;
        }
        return result;
    }

    /**
     * Is primitive check that includes big primitives.
     */
    public static boolean isPrimitive(Class<?> clazz) {
        return clazz.isPrimitive() ||
                clazz.equals(Boolean.class) ||
                clazz.equals(Byte.class) ||
                clazz.equals(Short.class) ||
                clazz.equals(Integer.class) ||
                clazz.equals(Long.class) ||
                clazz.equals(Float.class) ||
                clazz.equals(Double.class) ||
                clazz.equals(Character.class);
    }
}
