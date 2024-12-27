package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;


/**
 * <p>
 * 虚拟仓库存历史信息
 * </p>
 *
 * @author will
 * @since 2024-12-03
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("virtual_inventory_detail_his")
public class VirtualInventoryDetailHisEntity extends BaseEntity<VirtualInventoryDetailHisEntity> {

    /**
    * 快照日期
    */
    @TableField("date")
    private LocalDate date;
    /**
    * 数量
    */
    @TableField("qty")
    private Integer qty;
    /**
    * 虚拟仓流水id
    */
    @TableField("virtual_inventory_detail_id")
    private String virtualInventoryDetailId;

    /**
     * 批次剩余数量（平均库龄逆推）
     */
    @TableField("wait_qty")
    private Integer waitQty;

    /**
     * 库龄
     */
    @TableField("inventory_age_days")
    private Integer inventoryAgeDays;

    public static final String SKU_ID = "sku_id";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String VIRTUAL_WAREHOUSE_ID = "virtual_warehouse_id";

    public static final String DATE = "date";

    public static final String QTY = "qty";

    public static final String WAIT_QTY = "wait_qty";

    public static final String avgDays = "inventory_age_days";


    @Override
    public Serializable pkVal() {
        return null;
    }

}