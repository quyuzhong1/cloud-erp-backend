package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * <p>
 * 虚拟仓调整单明细表
 * </p>
 *
 * @author zdy
 * @since 2025-06-09
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("virtual_adjust_detail")
public class VirtualAdjustDetailEntity extends BaseEntity<VirtualAdjustDetailEntity> {

    /**
    * 主表id
    */
    @TableField("main_id")
    private String mainId;
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
    * 实收数量
    */
    @TableField("qty")
    private Integer qty;
    /**
    * 备注
    */
    @TableField("remark")
    private String remark;
    /**
    * 库存状态
    */
    @TableField("inventory_status")
    private String inventoryStatus;
    /**
    * 类型
     * InventoryInOutEnum
    */
    @TableField("type")
    private String type;
    /**
    * 产品名称
    */
    @TableField("product_name")
    private String productName;
    /**
    * 仓库id
    */
    @TableField("warehouse_id")
    private String warehouseId;
    /**
    * 虚拟仓id
    */
    @TableField("virtual_warehouse_id")
    private String virtualWarehouseId;
    /**
    * 虚拟仓库名称
    */
    @TableField("virtual_warehouse_name")
    private String virtualWarehouseName;
    /**
    * 仓库名称
    */
    @TableField("warehouse_name")
    private String warehouseName;


    public static final String MAIN_ID = "main_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String QTY = "qty";

    public static final String REMARK = "remark";

    public static final String DICT_INVENTORY_STATUS = "dict_inventory_status";

    public static final String TYPE = "type";

    public static final String PRODUCT_NAME = "product_name";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String VIRTUAL_WAREHOUSE_ID = "virtual_warehouse_id";

    public static final String VIRTUAL_WAREHOUSE_NAME = "virtual_warehouse_name";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    @Override
    public Serializable pkVal() {
        return null;
    }

}