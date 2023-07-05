package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 产品变体类型属性表
 * @TableName product_variant
 */
@TableName(value ="product_variant")
@Data
public class ProductVariantEntity extends BaseEntity implements Serializable {

    /**
     * 变体属性类型
     */
    @TableField(value = "property_type")
    private String propertyType;

    /**
     * 是否占用 默认 false  占用为true 就不能删除
     */
    @TableField(value = "occupy_status")
    private Boolean occupyStatus;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}