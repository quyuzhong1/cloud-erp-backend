package com.erp.model.mrp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * <p>
 * 库存表
 * </p>
 *
 * @author liaohui
 * @since 2024-09-23
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("local_history_inventory")
@EqualsAndHashCode(callSuper = true)
public class LocalHistoryInventoryEntity extends BaseEntity<LocalHistoryInventoryEntity> {

    private static final long serialVersionUID = 5962157482578744006L;
    /**
     * 组织id
     */
    @TableField("org_id")
    private String orgId;

    /**
     * 仓库id 
     */
    @TableField("warehouse_id")
    private String warehouseId;

    /**
     * 仓位id
     */
    @TableField("warehouse_location")
    private String warehouseLocation;

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
     * 库存状态（usable可用，frozen冻结，inTransit在途，waitQc待检）
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


    public static final String ORG_ID = "org_id";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String WAREHOUSE_LOCATION = "warehouse_location";

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
