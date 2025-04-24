package com.common.business.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.annotation.Annotation;
import java.util.Map;

@Slf4j
public abstract  class AbstractSparrowContext implements ApplicationContextAware, InitializingBean {
    protected ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * <p> Title: getBean
     * <p> Description: 获取spring容器中的对象
     *
     * @param targetClz 类
     *
     * @return T
     *
     * @author
     *
     */
    @SuppressWarnings("unchecked")
    protected <T> T getBean(Class<T> targetClz) {
        T beanInstance = null;
        //byType
        try {
            beanInstance = applicationContext.getBean(targetClz);
        } catch (Exception ignored) {
            log.warn("AbstractSparrowContext:首次获取失败:{}", targetClz);
        }
        //byName
        if (beanInstance == null) {
            String simpleName = targetClz.getSimpleName();
            simpleName = Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
            beanInstance = (T) applicationContext.getBean(simpleName);
        }
        return beanInstance;
    }

    /**
     * <p> Title: getBeanMapByAnnotation
     * <p> Description: 获取注解的类
     *
     * @param annotationClz 注解
     *
     * @return java.util.Map<java.lang.String,java.lang.Object>
     *
     * @author
     *
     */
    protected Map<String, Object> getBeanMapByAnnotation(Class<? extends Annotation> annotationClz) {
        return applicationContext.getBeansWithAnnotation(annotationClz);
    }
}
