package com.ming.server.ioc;

import com.ming.server.ioc.Bean;
import com.ming.server.ioc.factory.BeanFactory;
import com.ming.server.ioc.factory.impl.ReflectionBeanFactory;

import java.util.concurrent.ConcurrentHashMap;

public class SimpleIOC {
    private static final ConcurrentHashMap<Class<?>, Object> beanMap = new ConcurrentHashMap<>();

    // 默认使用反射工厂，可通过 setFactory() 方法替换
    private static BeanFactory beanFactory = new ReflectionBeanFactory();

    public static <T> T getBean(Class<T> clazz) {
        if (!clazz.isAnnotationPresent(Bean.class)) {
            throw new IllegalArgumentException("Class " + clazz.getName() + " is not a bean.");
        }
        return (T) beanMap.computeIfAbsent(clazz,key->beanFactory.createBean(clazz));
    }

    // 设置自定义工厂（可在应用启动时配置）,可以尝试其它的工厂进行创建
    public static void setFactory(BeanFactory factory) {
        beanFactory = factory;
    }

}