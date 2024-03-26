package com.common.business.annotation;


import com.common.business.enums.PlatformDictEnum;

import java.lang.annotation.*;

/**
 * 平台B2C订单模板模式注解
 *
 * @author Jim
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PlatformSoB2cAnnotate {
    PlatformDictEnum method();
}
