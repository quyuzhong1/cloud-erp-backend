package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import com.common.core.entity.BaseEntity;

/**
 * (CustomizeFieldDisplay)实体类
 *
 * @author yl
 * @since 2023-02-03 18:52:29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("customize_field_layout")
public class CustomizeFieldLayoutEntity extends BaseEntity<CustomizeFieldLayoutEntity> {

    /**
     * 字段名称
     */
    private String layoutJson;
    /**
     * 用户id
     */
    private String userId;

    /**
     * 页面模块编号
     */
    private String moduleCode  ;
    /**
     * 页面模块名称
     */
    private String moduleName  ;



}

