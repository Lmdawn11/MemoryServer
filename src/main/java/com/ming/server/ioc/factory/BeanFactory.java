package com.ming.server.ioc.factory;

public interface BeanFactory {
    <T> T createBean(Class<T> clazz);
}
