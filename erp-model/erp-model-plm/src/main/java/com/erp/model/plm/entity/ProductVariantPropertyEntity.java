package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 产品变体属性值表
 * @TableName product_variant_property
 */
@TableName(value ="product_variant_property")
@Data
public class ProductVariantPropertyEntity extends BaseEntity implements Serializable {

    /**
     * 属性值
     */
    @TableField(value = "property_value")
    private String propertyValue;

    /**
     * 属性值
     */
    @TableField(value = "property_code")
    private String propertyCode;

    /**
     * 变体类型表id
     */
    @TableField(value = "variant_id")
    private String variantId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}