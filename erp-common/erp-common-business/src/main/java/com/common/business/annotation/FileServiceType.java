package com.common.business.annotation;

import com.common.business.enums.FileServiceTypeEnum;
import com.common.business.enums.LogisticsPlatformEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FileServiceType {
    FileServiceTypeEnum value();
}
