package com.erp.model.plm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;

/**
 * 产品变体类型属性表
 * @TableName product_variant
 */
@Data
@NoArgsConstructor
public class ProductVariantDTO implements Serializable {

    /**
     * 主键id
     */
    private String id;

    /**
     * 变体属性类型
     */
    private String propertyType;

    /**
     * 变体值
     */
    private List<ProductVariantPropertyDTO> productVariantPropertyList;

    private static final long serialVersionUID = 1L;
}