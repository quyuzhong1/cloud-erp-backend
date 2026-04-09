package com.erp.model.sys.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import com.common.core.entity.BaseEntity;
import lombok.EqualsAndHashCode;

/**
 * @author Will
 * @version 1.0
 * @description: 业务编码表
 * @date 2022/11/21 11:24
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_code")
public class SysCodeEntity extends BaseEntity<SysCodeEntity> {

    /**
     * 主键id
     */
    @TableId(value = "id",type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 类目
     */
    @TableField("category")
    private String category;

    /**
     * 顺序码
     */
    @TableField("num")
    private Integer num;

    /**
     * 编码类型 (枚举SysNoEnum，1:sku,2:spu)
     */
    @TableField("type")
    private Integer type;

}
