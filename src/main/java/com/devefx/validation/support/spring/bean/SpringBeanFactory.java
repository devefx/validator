package com.devefx.validation.support.spring.bean;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.devefx.validation.Validator;
import com.devefx.validation.support.CacheImpl;
import com.devefx.validation.support.Interceptor;

public class SpringBeanFactory implements ApplicationContextAware {
    @Override
    public void setApplicationContext(final ApplicationContext context)
            throws BeansException {
        Interceptor.setCache(new CacheImpl() {
            @Override
            public Validator acquireInstance(
                    Class<? extends Validator> validClass) throws Exception {
                try {
                    return context.getBean(validClass);
                } catch (Exception e) {
                    return validClass.newInstance();
                }
            }
        });
    }
}
