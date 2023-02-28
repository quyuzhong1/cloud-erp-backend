package com.common.core.anno;

import java.lang.annotation.*;

@Target({ElementType.TYPE,ElementType.FIELD})
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
    boolean isNotBlank() default false;

    /**
     * 表达式正则，默认空，FieldFormatPatternTypeEnum枚举
     */
    String formatPattern() default "";

    /**
     * 最大长度，默认0
     */
    int maxLength() default 0;

    /**
     * 允许输入的值，逗号分隔
     */
    String fieldValues() default "";

    /**
     * 输入枚举Class,枚举必须要实现EnumMessage接口，并且字段一致
     */
    Class enumClass() default Enum.class;

    /**
     * 组别
     */
    Class<?>[] groups() default {};
}
