package com.erp.model.tms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;


/**
 * <p>
 * SKU成本明细
 * </p>
 *
 * @author zdy
 * @since 2024-08-16
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("inventory_sku_cost_detail")
public class InventorySkuCostDetailEntity extends BaseEntity<InventorySkuCostDetailEntity> {

    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
    /**
    * sku编码
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * skuId
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * 产品名称
    */
    @TableField("product_name")
    private String productName;
    /**
    * 单位（Pcs,ml）
    */
    @TableField("unit")
    private String unit;
    /**
    * 产品成本（6位小数）
    */
    @TableField("product_cost")
    private BigDecimal productCost;


    public static final String FIELD_REMARK = "remark";

    public static final String MAIN_ID = "main_id";

    public static final String SKU_NO = "sku_no";

    public static final String SKU_ID = "sku_id";

    public static final String PRODUCT_NAME = "product_name";

    public static final String FIELD_UNIT = "unit";

    public static final String PRODUCT_COST = "product_cost";

    @Override
    public Serializable pkVal() {
        return null;
    }

}