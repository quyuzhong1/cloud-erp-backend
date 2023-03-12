package com.erp.model.plm.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 产品采购备注信息列表（VO）
 * @TableName product_purchase_remark
 */
@Data
public class ProductPurchaseRemarkShowDTO implements Serializable {

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

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 创建人id
     */
    private String createUserId;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

    /**
     * 修改人id
     */
    private String updateUserId;

    private static final long serialVersionUID = 1L;
}