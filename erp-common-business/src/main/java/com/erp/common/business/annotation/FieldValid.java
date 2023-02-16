package com.erp.common.business.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FieldValid {

    int index() default 0;

    /**
     * 字段名称，默认为空
     */
    String fieldName() default "";

    /**
     * 是否必填项，默认false
     */
    boolean isNotNull() default false;

    /**
     * 表达式正则，默认空
     */
    String formatPattern() default "";

    /**
     * 最大长度，默认0
     */
    int maxLength() default 0;

    /**
     * 组别
     */
    Class<?>[] groups() default {};

    /**
     * 返回信息
     */
    String message() default "";
}
