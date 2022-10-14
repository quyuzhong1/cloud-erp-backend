package com.erp.common.annotation;

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

    // 默认错误消息
    String message() default "没有权限";

    String[] value();


}
