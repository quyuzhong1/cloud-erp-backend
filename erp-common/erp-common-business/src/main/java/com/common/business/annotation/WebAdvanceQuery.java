package com.common.business.annotation;

import com.common.business.query.IQueryHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 页面高级查询注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface WebAdvanceQuery {

    /**
     * 扩展字段处理类
     */
    Class<? extends IQueryHandler> handler() default IQueryHandler.class;
}
