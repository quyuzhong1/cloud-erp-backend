package com.common.core.utils;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.core.anno.FieldValid;
import com.common.core.constant.EnumMessage;
import com.common.core.enums.FieldFormatPatternTypeEnum;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
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
    public static List<String> fieldValid(Object object) {

        Field[] fields = object.getClass().getDeclaredFields();
        List<String> msgList = new ArrayList<>();
        for (Field field : fields) {
            //设置可访问
            field.setAccessible(true);
            //属性的值
            String fieldValue = null;
            try {
              Object obj =  field.get(object) ;
              fieldValue = ObjectUtils.isNotEmpty(obj) ? String.valueOf(obj) : "";
            } catch (IllegalAccessException e) {
                msgList.add(fieldValue + "输入有误！");
            }
            //判断字段是否含有注解
            boolean isExcelValid = field.isAnnotationPresent(FieldValid.class);
            if (isExcelValid) {
                FieldValid annotation = field.getAnnotation(FieldValid.class);
                String str = dataScopeFilter(annotation, fieldValue);
                if (StringUtils.isNotBlank(str)) {
                    msgList.add(str);
                }
            }

        }
        return msgList;
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
        boolean notNull = annotation.isNotBlank();
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
            msg.append(fieldName.concat("不能为空"));
            return msg.toString();
        }
        //长度校验
        if (length > 0 && StringUtils.isNotBlank(fieldValue)) {
            if (fieldValue.length() > length) {
                msg.append(fieldName.concat("长度不能大于").concat(String.valueOf(length)));
            }
        }
        //正则校验
        if (StringUtils.isNotBlank(formatPattern) && StringUtils.isNotBlank(fieldValue)) {
            //判断是否存在正则
            FieldFormatPatternTypeEnum fieldFormatPatternTypeEnum = FieldFormatPatternTypeEnum.getByCode(formatPattern);
            boolean matches;
            if (ObjectUtils.isNotEmpty(fieldFormatPatternTypeEnum)) {
                 matches = fieldValue.matches(fieldFormatPatternTypeEnum.getDesc());
                //枚举格式未匹配正确
                if (!matches) {
                    msg.append(fieldName.concat("[").concat(fieldFormatPatternTypeEnum.getName()).concat("]格式不正确"));
                }
            } else {
                 matches = fieldValue.matches(formatPattern);
                //格式未匹配正确
                if (!matches) {
                    msg.append(fieldName.concat("格式不正确"));
                }
            }
        }
        //固定值校验
        if (StringUtils.isNotBlank(fieldValues) && StringUtils.isNotBlank(fieldValue)) {
            List<String> valueList = Arrays.stream(fieldValues.split(",")).collect(Collectors.toList());
            if (!valueList.contains(fieldValue)){
                msg.append(fieldName.concat("输入值必须为[".concat(fieldValues).concat("]")));
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
                    msg.append(fieldName.concat("输入值不匹配"));
                }
            }
        }
        return msg.toString();
    }

    /**
     * @description: 导出错误数据添加需号
     * @author Will
     * @date: 2023/2/28 10:15
     * @param errorMsgList
     * @return String
     */
    public static String getMsgSort(List<String> errorMsgList) {
        String errStr = "";
        //如果错误数据为空则返回
        if (CollectionUtils.isEmpty(errorMsgList)) {
            return errStr;
        }
        for (int i = 0; i < errorMsgList.size(); i++) {
            Integer indexTemp = i + 1;
            errStr = errStr + indexTemp + "、" + errorMsgList.get(i) + "；";
        }
        return errStr;
    }
}
