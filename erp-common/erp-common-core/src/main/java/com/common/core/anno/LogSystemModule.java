package com.common.core.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 日志系统模块名称
 *
 * @author Jim
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LogSystemModule {

    /**
     * 默认:空=解析当前服务模块的名称
     *
     * @return 系统模块名称
     */
    String value() default "";
}