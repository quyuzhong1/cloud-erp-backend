package com.cloud.erp.chrome.annotation;

import com.cloud.erp.chrome.aspect.IsContainValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

/**
 * @Classname IsContain
 * @Description TODO
 * @Date 2022-08-29 14:43
 * @Created by yl
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {IsContainValidator.class})
public @interface IsContain {

    String message() default "类型错误";


    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
