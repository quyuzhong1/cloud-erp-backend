package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * bom 与sku关系表(BomRefSku)实体类
 *
 * @author yl
 * @since 2023-01-09 11:32:05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("product_bom_sku")
public class BomSkuEntity extends BaseEntity<BomSkuEntity> implements Serializable {
    private static final long serialVersionUID = -92121071004214361L;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 父级id '0' 是第一级
     */
    private String parentSkuNo;

    /**
     * sku
     */
    private String skuNo;

    /**
     * skuId
     */
    private String skuId;

    /**
     * bom 表id
     */
    private String bomId;

    /**
     * 父级表skuid
     */
    private String parentSkuId;

    /**
     *产品id
     */
    private String productId;

}

