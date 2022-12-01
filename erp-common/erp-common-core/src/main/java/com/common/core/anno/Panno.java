package com.common.core.anno;

import com.common.core.enums.PannoEnum;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface Panno {
    PannoEnum[] findType() default {PannoEnum.EQ};

    String field() default "";
}

