package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * B2B客户装箱（行表，当前关联 B2B三方发货单 b2b_third_delivery）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("b2b_customer_packing")
public class B2bCustomerPackingEntity extends BaseEntity<B2bCustomerPackingEntity> {

    @TableField("main_id")
    private String mainId;

    @TableField("box_seq")
    private Integer boxSeq;

    @TableField("box_mark_no")
    private String boxMarkNo;

    @TableField("box_mark_ref_no")
    private String boxMarkRefNo;

    @TableField("label_size")
    private String labelSize;

    @TableField("labeling_requirement")
    private String labelingRequirement;

    @TableField("sku_id")
    private String skuId;

    @TableField("sku_no")
    private String skuNo;

    @TableField("product_name")
    private String productName;

    @TableField("sale_qty")
    private Integer saleQty;

    @TableField("packing_qty")
    private Integer packingQty;

    @TableField("warehouse_platform_sku")
    private String warehousePlatformSku;

    @TableField("sort")
    private Integer sort;

    @Override
    public Serializable pkVal() {
        return this.getId();
    }
}
