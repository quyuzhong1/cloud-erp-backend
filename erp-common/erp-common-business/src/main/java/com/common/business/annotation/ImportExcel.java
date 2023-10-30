package com.common.business.annotation;


import java.lang.annotation.*;

/**
 * 导入标签
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ImportExcel {
    String handlerName() default "";
}
