package com.common.core.anno;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.EnumUtil;
import com.alibaba.excel.util.StringUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @Classname StateEnumValueValidator
 * @Description TODO
 * @Date 2022-09-15 10:16
 * @Created by yl
 */
@Slf4j
public class StateEnumValueValidator implements ConstraintValidator<StateEnumValue, Object> {

    // 默认枚举类名称
    private static final String DEFAULT_CLAZZ_NAME = "Object";

    private String[] strValues;
    private int[] intValues;
    private Class clazz;
    private String enumCheckField;

    @Override
    public void initialize(StateEnumValue constraintAnnotation) {
        strValues = constraintAnnotation.strValues();
        intValues = constraintAnnotation.intValues();
        this.clazz = constraintAnnotation.clazz();
        this.enumCheckField = constraintAnnotation.enumCheckField();
    }

    @SneakyThrows
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext constraintValidatorContext) {
        /**
         * 此处为了保证兼容，优先级strValues和intValues高于clazz
         * 为空不验证，有单独的非空验证
         */

        if (StringUtils.isEmpty(value)) {
            return true;
        }
        if (ArrayUtil.isNotEmpty(this.strValues)) {
            if (value instanceof String) {
                return Convert.toList(this.strValues).contains(value);
            }
        } else if (ArrayUtil.isNotEmpty(this.intValues)) {
            if (value instanceof Integer) {
                return Convert.toList(this.intValues).contains(((Integer) value).intValue());
            }
        } else { // 走枚举类验证
            // 判断是否默认值
            String enumClazzName = this.clazz.getSimpleName();
            if (Objects.equals(enumClazzName, DEFAULT_CLAZZ_NAME)) {
                log.error("未定义枚举类类型，无法验证");
                return false;
            }
            if (!EnumUtil.isEnum(this.clazz)) {
                log.error("枚举类类型配置错误，无法验证");
                return false;
            }
            //枚举类统一用code
            Map<String, Object> enumMap = EnumUtil.getNameFieldMap(clazz, this.enumCheckField);
            if (CollectionUtil.isNotEmpty(enumMap)) {
                List<Object> valueList = new ArrayList<>(enumMap.values());
                Object obj = getGetMethodByField(value, this.enumCheckField);
                return valueList.contains(obj);
            }
            return false;
        }
        return false;
    }


    /**
     * 根据字段获取到对应的get方法的值
     *
     * @param obj
     * @param field
     * @return
     */
    private Object getGetMethodByField(Object obj, String field) throws InvocationTargetException, IllegalAccessException {
        Method[] methods = obj.getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            if (("get" + field).toLowerCase().equals(methods[i].getName().toLowerCase())) {
                return methods[i].invoke(obj);
            }
        }
        return null;
    }
}
