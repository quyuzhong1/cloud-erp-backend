package com.common.business.annotation;



import com.common.business.enums.PlatformApiEnum;

import java.lang.annotation.*;

/**
 * 平台数据拉取对接模板模式注解
 * @author Cloud
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SaveData {
    PlatformApiEnum method();
}
