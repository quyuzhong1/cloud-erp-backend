package com.erp.common.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataPermision {
    /**
     * 部门表的别名
     */
    String field() default "";

    /**
     * 用户表的别名
     */
    String menuCode() default "";

    int index() default 0;

    String param() default "param";

    //这个是给那个字段赋值
    String dataScope() default "dataScope";
}
