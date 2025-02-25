package com.ming.server.ioc;

import com.ming.server.config.A;
import com.ming.server.config.B;
import com.ming.server.config.SetConfig;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SimpleIOC {
    private static final ConcurrentHashMap<Class<?>, Object> BEAN_MAP = new ConcurrentHashMap<>();

    /**
     * 实现静态代码块，加载的时候执行
     * 手动注册Bean
     */
    static {
        try {
            registerBean(SetConfig.class);
        }catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }

    /**
     * 注册bean
     * @param beanClasses
     */
    private static void registerBean(Class<?>... beanClasses) {
        //1 先创建对象，但不初始化，防止循环依赖
        for (Class<?> beanClass : beanClasses) {
            if (!beanClass.isAnnotationPresent(Bean.class)) continue;  //判断是否有注解标记

            if (!BEAN_MAP.containsKey(beanClass)){
                BEAN_MAP.put(beanClass,createInstance(beanClass));  //创建但不初始化
            }
        }

        //2.执行构造方法，注入依赖
        for (Class<?> beanClass : beanClasses) {
            if (!beanClass.isAnnotationPresent(Bean.class)) continue;   //无bean注解 直接跳过，

            Object instance = BEAN_MAP.get(beanClass);
            Constructor<?> constructor = beanClass.getDeclaredConstructors()[0];

            Class<?>[] parameterTypes = constructor.getParameterTypes();
            Object[] objects = new Object[parameterTypes.length];

            for (int i = 0; i < parameterTypes.length; i++) {
                objects[i] = BEAN_MAP.get(parameterTypes[i]);  // **从 BEAN_MAP 获取依赖**
            }

            //调用构造方法，注入依赖
            constructor.setAccessible(true);
            try {
                instance = constructor.newInstance(parameterTypes);
                BEAN_MAP.put(beanClass, instance);  //更新map
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

    }

    /**
     * 创建bean实例，但不初始化
     * @param clazz
     * @return
     */
    private static Object createInstance(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            log.info("创建bean实例失败,error:{}",e.getMessage());
        }
        return null;
    }

    public static <T> T getBean(Class<T> clazz) {
        return (T) BEAN_MAP.get(clazz);
    }
}
