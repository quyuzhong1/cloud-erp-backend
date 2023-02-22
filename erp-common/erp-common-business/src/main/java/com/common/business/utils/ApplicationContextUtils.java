package com.common.business.utils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @Classname ApplicationContextUtils
 * @Description TODO
 * @Date 2022-10-19 18:30
 * @Created by yl
 */

@Component
public class ApplicationContextUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    //方式二:传bean对象
    //已经new出来的对象进行注入
    public static <T> T autowire(T bean) {
        applicationContext.getAutowireCapableBeanFactory()
                .autowireBeanProperties(bean, AutowireCapableBeanFactory.AUTOWIRE_AUTODETECT, true);
        return bean;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextUtils.applicationContext = applicationContext;

    }
}
