package com.erp.model.plm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.common.core.entity.BaseEntity;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 产品采购信息备注表
 * @TableName product_purchase_remark
 */
@TableName(value ="product_purchase_remark")
@Data
public class ProductPurchaseRemarkEntity extends BaseEntity implements Serializable {

    /**
     * 产品信息表id
     */
    @TableField(value = "product_id")
    private String productId;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}