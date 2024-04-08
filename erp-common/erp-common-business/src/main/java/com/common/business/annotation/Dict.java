package com.common.business.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.common.business.enums.ServiceCodeNameEnum;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Dict {
	/**
     * 字符串累加时需要指定enum 支持多选，
     */
    String enumClass() default "";
    
    /**
     * 方法描述:  条件字段名
     *
     * @return 返回类型： String
     */
    String queryFieldName() default "value";

    /**
     * 方法描述:  返回字典属性名，多个逗号分隔
     *
     * @return 返回类型： String
     */
    String returnFieldName() default "name";

    /**
     * 方法描述: 数据字典表
     *
     * @return 返回类型： String
     */
    String tableName() default "dict_basic";


    /**
     * 字典表所在的服务
     * @return
     */
    ServiceCodeNameEnum serviceCode() default ServiceCodeNameEnum.DEFAULT;
    
    /**
     * 是否翻译子属性里的字典字段
     *
     * @return
     */
    boolean dictChildren() default false;


    /**
     * 翻译为空是否取原值
     *
     * @return
     */
    boolean dictDefaultOriginalValue() default false;

}
