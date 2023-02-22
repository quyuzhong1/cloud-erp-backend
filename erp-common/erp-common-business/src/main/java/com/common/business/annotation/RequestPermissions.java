package com.common.business.annotation;

import java.lang.annotation.*;

/** 请求权限
 * @Classname RequestPermissions
 * @Description TODO
 * @Date 2022-10-14 16:33
 * @Created by yl
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestPermissions {


    /**
     * 要注入的参数索引，一般为第一个，极端情况下会使用多个
     *
     * @return
     */
    int index() default 0;

    String value() default "";

    //这个是给那个字段赋值
    String dataScope() default "dataScope";


}
