package com.erp.common.business.filter;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.erp.common.business.annotation.FieldValid;

import java.lang.reflect.Field;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/14 18:17
 */
public class FieldValidFilter {

    public static String valid(Object object) {
        Field[] fields = object.getClass().getDeclaredFields();
        for (Field field : fields) {
            //设置可访问
            field.setAccessible(true);
            //属性的值
            Object fieldValue = null;
            try {
                fieldValue = field.get(object);
            } catch (IllegalAccessException e) {
                return fieldValue + "输入有误！";
            }
            //判断字段是否含有注解
            boolean isExcelValid = field.isAnnotationPresent(FieldValid.class);
            if (isExcelValid) {
                FieldValid annotation = field.getAnnotation(FieldValid.class);
                dataScopeFilter(annotation, String.valueOf(fieldValue));
            }

        }
        return "";
    }

    private static String dataScopeFilter(FieldValid annotation, String fieldValue) {
        //是否必填
        boolean notNull = annotation.isNotNull();
        //长度
        int length = annotation.maxLength();
        //类型/正则
        String formatPattern = annotation.formatPattern();
        String msg = "";

        if (notNull) {
            if (StringUtils.isBlank(fieldValue)) {
                msg = "";
            }
        }
        return msg.toString();
    }
}
