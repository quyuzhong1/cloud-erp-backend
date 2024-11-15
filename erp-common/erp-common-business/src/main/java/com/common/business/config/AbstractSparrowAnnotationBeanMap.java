package com.common.business.config;


import com.google.common.collect.Maps;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.util.Map;

public abstract class AbstractSparrowAnnotationBeanMap<A extends Annotation,B> extends AbstractSparrowContext {
    /**
     * <p> Title: getAnnotation
     * <p> Description: 需要缓存的注解类型
     *
     * @return java.lang.Class<A>
     *
     * @author 
     *
     */
    public abstract Class<A> getAnnotation();
    /**
     * <p> Title: refresh
     * <p> Description: 暴露给实现类去操作刷新earlyBeans的接口
     *
     * @author 
     *
     */
    public abstract void refresh(Map<A, B> annotationBeanMap);

    @Override
    @SuppressWarnings("unchecked")
    public void afterPropertiesSet() throws Exception {
        Map<A, B> annotationBeanMap = Maps.newHashMap();
        Map<String, Object> beanMap = getBeanMapByAnnotation(getAnnotation());
        beanMap.values().forEach(bean -> {
            A annotation = AnnotationUtils.findAnnotation(bean.getClass(), getAnnotation());
            annotationBeanMap.put(annotation, (B) bean);
        });
        refresh(annotationBeanMap);
    }
}
