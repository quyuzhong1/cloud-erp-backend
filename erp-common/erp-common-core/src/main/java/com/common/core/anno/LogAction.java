package com.common.core.anno;

import com.common.core.enums.LogActionEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解标记
 *
 * @author Jim
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogAction {

    /**
     * 操作行为
     * {@link LogActionEnum}
     */
    LogActionEnum value();

    /**
     * 操作描述
     */
    String desc();

    /**
     * 操作请求的主键字段名
     * 默认: 单操作默认=id, 批量操作=ids
     */
    String keyIdName() default "";


    /**
     * 操作请求的主键字段名
     * 默认: 单操作默认=id, 批量操作=ids
     */
    String isBatchOperationStr() default "";
}