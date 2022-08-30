package com.cloud.erp.chrome.aspect;

import com.cloud.erp.chrome.annotation.IsContain;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @Classname IsContainValidator
 * @Description TODO
 * @Date 2022-08-29 14:50
 * @Created by yl
 */
public class IsContainValidator implements ConstraintValidator<IsContain, String> {
    private String flagString;

    @Override
    public void initialize(IsContain constraintAnnotation) {

    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return false;
    }
}
