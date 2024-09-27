package com.erp.model.mrp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * <p>
 * 虚拟库存表
 * </p>
 *
 * @author liaohui
 * @since 2024-09-27
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("virtual_inventory_history")
public class VirtualInventoryHistoryEntity extends BaseEntity<VirtualInventoryHistoryEntity> {

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
     * 单据日期
     */
    @TableField("bill_date")
    private LocalDate billDate;


    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String VIRTUAL_WAREHOUSE_ID = "virtual_warehouse_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String DICT_INVENTORY_STATUS = "dict_inventory_status";

    public static final String QTY = "qty";

    public static final String BILL_DATE = "bill_date";

    @Override
    public Serializable pkVal() {
        return null;
    }

}
