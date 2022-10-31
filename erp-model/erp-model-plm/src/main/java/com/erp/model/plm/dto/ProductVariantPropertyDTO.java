package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * 产品变体属性值表
 * @TableName product_variant_property
 */
@Data
@NoArgsConstructor
public class ProductVariantPropertyDTO implements Serializable {

    /**
     * 主键id
     */
    private String id;

    /**
     * 属性值
     */
    private String propertyValue;

    /**
     * 变体类型表id
     */
    private String variantId;

    private static final long serialVersionUID = 1L;
}