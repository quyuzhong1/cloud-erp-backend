package com.common.core.anno;

/**
 * @Classname StateEnumValue
 * @Description TODO
 * @Date 2022-09-15 10:12
 * @Created by yl
 */

import com.baomidou.mybatisplus.annotation.EnumValue;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Administrator
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.PARAMETER})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {StateEnumValueValidator.class})
public @interface StateEnumValue {


    // 默认错误消息
    String message() default "必须为指定值";

    String[] strValues() default {};

    int[] intValues() default {};

    /**
     * 指定枚举类，为保证兼容，优先验证strValues和intValues的值
     * @return
     */
    Class<?> clazz() default Object.class;

    // 分组
    Class<?>[] groups() default {};

    // 负载
    Class<? extends Payload>[] payload() default {};

    // 指定多个时使用
    @Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
    @Retention(RUNTIME)
    @Documented
    @interface List {
        EnumValue[] value();
    }

}
