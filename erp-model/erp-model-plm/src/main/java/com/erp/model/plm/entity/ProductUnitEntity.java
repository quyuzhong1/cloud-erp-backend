package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;

import java.io.Serializable;

/**
 * 
 * @TableName product_unit
 */
@TableName(value ="product_unit")
@Data
public class ProductUnitEntity extends BaseEntity<ProductUnitEntity> implements Serializable {

    /**
     * 单位名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 是否占用 默认 false  占用为true 就不能删除
     */
    @TableField(value = "occupy_status")
    private Boolean occupyStatus;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}