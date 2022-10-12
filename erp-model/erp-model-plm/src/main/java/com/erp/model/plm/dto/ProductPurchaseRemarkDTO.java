package com.erp.model.plm.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 产品采购备注信息列表（VO）
 * @TableName product_purchase_remark
 */
@Data
public class ProductPurchaseRemarkDTO implements Serializable {

    /**
     * 主键id
     */
    private String id;

    /**
     * 产品采购信息表id
     */
    private String productId;

    /**
     * 备注
     */
    private String remark;

    private static final long serialVersionUID = 1L;
}