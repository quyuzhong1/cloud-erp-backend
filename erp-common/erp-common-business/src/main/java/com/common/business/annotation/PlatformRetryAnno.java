package com.common.business.annotation;



import com.common.business.enums.PlatformDictEnum;

import java.lang.annotation.*;

/**
 * 平台异常重新同步模板模式注解
 *
 * @author Jim
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PlatformRetryAnno {
    PlatformDictEnum method();
}
