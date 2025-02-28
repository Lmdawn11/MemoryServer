package com.ming.server.ioc;

import com.ming.server.ioc.Bean;

import java.util.concurrent.ConcurrentHashMap;

public class SimpleIOC {
    private static final ConcurrentHashMap<Class<?>, Object> beanMap = new ConcurrentHashMap<>();

    public static <T> T getBean(Class<T> clazz) {
        if (!clazz.isAnnotationPresent(Bean.class)) {
            throw new IllegalArgumentException("Class " + clazz.getName() + " is not a bean.");
        }
        return (T) beanMap.computeIfAbsent(clazz, key -> {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Failed to create instance of " + clazz.getName(), e);
            }
        });
    }
}