package com.erp.model.plm.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 产品采购备注信息列表（VO）
 * @TableName product_purchase_remark
 */
@Data
public class ProductPurchaseRemarkDTO implements Serializable {

    /**
     * 主键id
     */
    @ApiModelProperty(value = "主键id")
    private String id;

    /**
     * 产品采购信息表id
     */
    @ApiModelProperty(value = "产品信息表id")
    private String productId;

    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;

    private static final long serialVersionUID = 1L;
}