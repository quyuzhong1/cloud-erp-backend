package com.common.business.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.common.business.enums.ServiceCodeNameEnum;
import com.common.core.constant.EnumMessage;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Dict {
	/**
     * 字符串累加时需要指定enum，
     */
	Class<? extends EnumMessage> enumClass() default EnumMessage.class;
    
    /**
     * 方法描述:  条件字段名
     *{@code @BaseDataMapper(basePackages = "com.common.business.mapper")}
     * @return 返回类型： String
     */
    String queryFieldName() default "value";
    
    /**
     * 方法描述:  查询type字段条件 ，表没有type字段勿添加
     *{@code @BaseDataMapper(basePackages = "com.common.business.mapper")}
     * @return 返回类型： String
     */
    String queryTypeField() default "";
    
    /**
     * 方法描述: 扩展条件查询sql片段
     * {@code @BaseDataMapper(basePackages = "com.common.business.mapper")}
     * @return 返回类型： String
     */
    String extendQuerySql() default "";

    /**
     * 方法描述:  返回字典属性名，多个逗号分隔
     *{@code @BaseDataMapper(basePackages = "com.common.business.mapper")}
     * @return 返回类型： String
     */
    String returnFieldName() default "name";

    /**
     * 方法描述: 数据字典表
     *{@code @BaseDataMapper(basePackages = "com.common.business.mapper")}
     * @return 返回类型： String
     */
    String tableName() default "dict_basic";


    /**
     * 字典表所在的服务
     * {com.common.business.aspect.DictCore.getBaseDataFeign(ServiceCodeNameEnum)}
     * @return
     */
    ServiceCodeNameEnum serviceCode() default ServiceCodeNameEnum.DEFAULT;
    
    /**
     * 翻译为空是否取原值
     *
     * @return
     */
    boolean dictDefaultOriginalValue() default false;

}
