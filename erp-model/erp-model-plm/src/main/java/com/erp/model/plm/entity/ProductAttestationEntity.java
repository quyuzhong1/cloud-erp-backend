package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 产品认证信息表(ProductAttestation)实体类
 *
 * @author yl
 * @since 2023-02-25 12:32:04
 */
@TableName("product_attestation")
@Data
@NoArgsConstructor
public class ProductAttestationEntity extends BaseEntity {


    /**
     * sku Id
     */
    private String skuId;
    /**
     * 产品认证
     */
    private String product;
    /**
     * 运输认证
     */
    private String transport;
    /**
     * 其他认证
     */
    private String other;



}

