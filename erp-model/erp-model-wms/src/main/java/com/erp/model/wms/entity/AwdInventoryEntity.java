package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 
 * </p>
 *
 * @author wtr
 * @since 2025-12-26
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@TableName("awd_inventory")
public class AwdInventoryEntity extends BaseEntity<AwdInventoryEntity> {

    /**
    * 仓库id
    */
    @TableField("warehouse_id")
    private String warehouseId;
    /**
    * 仓库名称
    */
    @TableField("warehouse_name")
    private String warehouseName;
    /**
    * 平台产品id
    */
    @TableField("asin")
    private String asin;
    /**
    * 平台sku
    */
    @TableField("msku")
    private String msku;
    /**
    * fnsku
    */
    @TableField("fnsku")
    private String fnsku;
    /**
    * skuId
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * sku编码
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 产品名称
    */
    @TableField("product_name")
    private String productName;
    /**
    * AWD在库
    */
    @TableField("total_onhand_qty")
    private Integer totalOnhandQty;
    /**
    * AWD可用
    */
    @TableField("available_distributable_qty")
    private Integer availableDistributableQty;
    /**
    * AWD发FBA在途
    */
    @TableField("replenishment_qty")
    private Integer replenishmentQty;
    /**
    * AWD待发货
    */
    @TableField("reserved_distributable_qty")
    private Integer reservedDistributableQty;
    /**
    * 发AWD在途
    */
    @TableField("total_inbound_qty")
    private Integer totalInboundQty;


    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String WAREHOUSE_NAME  = "warehouse_name ";

    public static final String ASIN = "asin";

    public static final String MSKU = "msku";

    public static final String FNSKU = "fnsku";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String PRODUCT_NAME = "product_name";

    public static final String TOTAL_ONHAND_QTY = "total_onhand_qty";

    public static final String AVAILABLE_DISTRIBUTABLE_QTY = "available_distributable_qty";

    public static final String REPLENISHMENT_QTY = "replenishment_qty";

    public static final String RESERVED_DISTRIBUTABLE_QTY = "reserved_distributable_qty";

    public static final String TOTAL_INBOUND_QTY = "total_inbound_qty";

    @Override
    public Serializable pkVal() {
        return null;
    }

}