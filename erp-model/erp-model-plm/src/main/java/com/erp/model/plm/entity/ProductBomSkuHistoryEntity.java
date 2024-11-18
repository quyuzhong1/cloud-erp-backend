package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * bom历史表与sku关系表(ProductBomSkuHistory)实体类
 *
 * @author yl
 * @since 2023-01-11 12:26:09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("product_bom_sku_history")
public class ProductBomSkuHistoryEntity extends BaseEntity<ProductBomSkuHistoryEntity> implements Serializable {
    private static final long serialVersionUID = -19422095845826564L;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 父级sku_no '0' 是第一级
     */
    private String parentSkuNo;

    /**
     * sku编号
     */
    private String skuNo;

    /**
     * bom 历史表id
     */
    private String bomHistoryId;

    /**
     * 父级表skuid
     */
    private String parentSkuId;

    /**
     * skuId
     */
    private String skuId;

    /**
     *产品id
     */
    private String productId;
}

