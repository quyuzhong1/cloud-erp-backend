package com.erp.common.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
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
}
