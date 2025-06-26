package com.erp.model.scm.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 供应商采购数量
 * </p>
 *
 * @author jack
 * @since 2025-06-18
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("supplier_purchase_quantity")
public class SupplierPurchaseQuantityEntity extends BaseEntity<SupplierPurchaseQuantityEntity> {

    /**
    * 供应商id
    */
    @TableField("supplier_id")
    private String supplierId;
    /**
    * 供应商名称
    */
    @TableField("supplier_name")
    private String supplierName;
    /**
    * sku_id
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * sku_no
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 供应商数量
    */
    @TableField("supplier_qty")
    private Integer supplierQty;
    /**
    * sku总数量
    */
    @TableField("sku_total_qty")
    private Integer skuTotalQty;
    /**
    * 采购比例
    */
    @TableField("purchase_ratio")
    private BigDecimal purchaseRatio;


    public static final String SUPPLIER_ID = "supplier_id";

    public static final String SUPPLIER_NAME = "supplier_name";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String SUPPLIER_QTY = "supplier_qty";

    public static final String SKU_TOTAL_QTY = "sku_total_qty";

    public static final String PURCHASE_RATIO = "purchase_ratio";

    @Override
    public Serializable pkVal() {
        return null;
    }

}