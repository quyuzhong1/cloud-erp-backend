package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * <p>
 * 虚拟库存表
 * </p>
 *
 * @author will
 * @since 2024-06-03
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("virtual_inventory")
public class VirtualInventoryEntity extends BaseEntity<VirtualInventoryEntity> {

    /**
    * 仓库id 
    */
    @TableField("warehouse_id")
    private String warehouseId;
    /**
    * 虚拟仓库id
    */
    @TableField("virtual_warehouse_id")
    private String virtualWarehouseId;
    /**
    * sku id
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * sku编号
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 库存状态（usable可用，frozen冻结）
    */
    @TableField("dict_inventory_status")
    private String dictInventoryStatus;
    /**
    * 数量
    */
    @TableField("qty")
    private Integer qty;


    /**
     * 操作后库存数量
     */
    @TableField(exist = false)
    private Integer afterQty;

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String VIRTUAL_WAREHOUSE_ID = "virtual_warehouse_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String DICT_INVENTORY_STATUS = "dict_inventory_status";

    

    @Override
    public Serializable pkVal() {
        return null;
    }

}