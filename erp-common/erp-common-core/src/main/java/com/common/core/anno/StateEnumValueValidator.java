package com.common.core.anno;


import com.common.core.anno.StateEnumValue;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * @Classname StateEnumValueValidator
 * @Description TODO
 * @Date 2022-09-15 10:16
 * @Created by yl
 */
public class StateEnumValueValidator  implements ConstraintValidator<StateEnumValue,Object> {

    private String[] strValues;
    private int[] intValues;

    @Override
    public void initialize(StateEnumValue constraintAnnotation) {
        strValues = constraintAnnotation.strValues();
        intValues = constraintAnnotation.intValues();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {
        if (value instanceof String) {
            for (String str : strValues) {
                if (str.equals(value)) {
                    return true;
                }
            }
        } else if (value instanceof Integer) {
            for (int s : intValues) {
                if (s == ((Integer) value).intValue()) {
                    return true;
                }
            }
        }
        return false;
    }
}
