package com.common.core.utils;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.core.anno.FieldValid;
import com.common.core.constant.EnumMessage;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/2/14 18:17
 */
@Slf4j
public class FieldValidUtil {

    /**
     * @description: 字段验证调用
     * @author Will
     * @date: 2023/2/17 15:27
     * @param object
     * @return String
     */
    public static String fieldValid(Object object) {

        Field[] fields = object.getClass().getDeclaredFields();
        StringBuilder msg = new StringBuilder();
        for (Field field : fields) {
            //设置可访问
            field.setAccessible(true);
            //属性的值
            String fieldValue = null;
            try {
              Object obj =  field.get(object) ;
              fieldValue = ObjectUtils.isNotEmpty(obj) ? String.valueOf(obj) : "";
            } catch (IllegalAccessException e) {
                return fieldValue + "输入有误！";
            }
            //判断字段是否含有注解
            boolean isExcelValid = field.isAnnotationPresent(FieldValid.class);
            if (isExcelValid) {
                FieldValid annotation = field.getAnnotation(FieldValid.class);
                String str = dataScopeFilter(annotation, fieldValue);
                if (StringUtils.isNotBlank(str)) {
                    msg.append(str);
                }
            }

        }
        return msg.toString();
    }

    /**
     * @description: 字段验证
     * @author Will
     * @date: 2023/2/17 15:27
     * @param annotation
     * @param fieldValue
     * @return String
     */
    private static String dataScopeFilter(FieldValid annotation, String fieldValue) {
        //字段名称
        String fieldName = annotation.fieldName();
        //是否必填
        boolean notNull = annotation.isNotNull();
        //长度
        int length = annotation.maxLength();
        //类型/正则
        String formatPattern = annotation.formatPattern();
        //可输入的值
        String fieldValues = annotation.fieldValues();
        //枚举class
        Class enumClass = annotation.enumClass();

        StringBuilder msg = new StringBuilder();

        //必填校验
        if (notNull && StringUtils.isBlank(fieldValue)) {
            msg.append(fieldName.concat("不能为空;"));
            return msg.toString();
        }
        //长度校验
        if (length > 0 && StringUtils.isNotBlank(fieldValue)) {
            if (fieldValue.length() > length) {
                msg.append(fieldName.concat("长度不能大于").concat(String.valueOf(length)).concat(";"));
            }
        }
        //正则校验
        if (StringUtils.isNotBlank(formatPattern) && StringUtils.isNotBlank(fieldValue)) {
            //判断是否存在正则
            FieldFormatPatternTypeEnum fieldFormatPatternTypeEnum = FieldFormatPatternTypeEnum.getByName(formatPattern);
            boolean matches;
            if (ObjectUtils.isNotEmpty(fieldFormatPatternTypeEnum)) {
                 matches = fieldValue.matches(fieldFormatPatternTypeEnum.getDesc());
            } else {
                 matches = fieldValue.matches(formatPattern);
            }
            //格式未匹配正确
            if (!matches) {
                msg.append(fieldName.concat("格式不正确;"));
            }
        }
        //固定值校验
        if (StringUtils.isNotBlank(fieldValues) && StringUtils.isNotBlank(fieldValue)) {
            List<String> valueList = Arrays.stream(fieldValues.split(",")).collect(Collectors.toList());
            if (!valueList.contains(fieldValue)){
                msg.append(fieldName.concat("输入值必须为[".concat(fieldValues).concat("];")));
            }
        }
        //枚举值校验
        if (!Enum.class.equals(enumClass) && StringUtils.isNotBlank(fieldValue)) {
            Method method = null;
            EnumMessage[] messages = null;
            try {
                method = enumClass.getMethod("values");
                messages = (EnumMessage[]) method.invoke(null, null);
            } catch (Exception e) {
                log.error("枚举转化失败",e);
            }
            if (messages != null) {
                List<String> nameList = Arrays.stream(messages).map(EnumMessage::getName).collect(Collectors.toList());
                if (!nameList.contains(fieldValue)) {
                    msg.append(fieldName.concat("输入值不匹配;"));
                }
            }
        }
        return msg.toString();
    }
}
