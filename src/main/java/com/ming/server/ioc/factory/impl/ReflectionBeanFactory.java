package com.ming.server.ioc.factory.impl;

import com.ming.server.ioc.factory.BeanFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReflectionBeanFactory implements BeanFactory {
    @Override
    public <T> T createBean(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        }catch(Exception e) {
            log.error("clazz:{} 对象工厂创建失败error:{}",clazz,e.getMessage());
        }
        return null;
    }
}
