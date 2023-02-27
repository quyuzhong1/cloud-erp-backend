package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 产品包装/辅料信息(ProductAccessories)实体类
 *
 * @author yl
 * @since 2023-02-25 12:32:03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("product_accessories")
public class ProductAccessoriesEntity extends BaseEntity {
    private static final long serialVersionUID = 203604435236054219L;

    /**
     * 父级sku id
     */
    private String parentSkuId;
    /**
     * 数量
     */
    private Integer quantity;
    /**
     * 辅料的sku id
     */
    private String accessoriesSkuId;

    /**
     * 产品id
     */
    private String productId;



}

