package com.common.business.annotation;

import com.common.business.enums.SourceTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 来源类型注解
 * @author will
 * @date 2025/6/18 09:54
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ApproveBusinessKey {
    SourceTypeEnum value();
}
