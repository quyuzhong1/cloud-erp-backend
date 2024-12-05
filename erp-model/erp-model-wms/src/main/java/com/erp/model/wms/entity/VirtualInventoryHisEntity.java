package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
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
@TableName("virtual_inventory_his")
public class VirtualInventoryHisEntity extends BaseEntity<VirtualInventoryHisEntity> {

    /**
    * skuId
    */
    @TableField("sku_id")
    private String skuId;
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
    * 库存状态
    */
    @TableField("dict_inventory_status")
    private String dictInventoryStatus;
    /**
    * 虚拟仓流水id
    */
    @TableField("virtual_trans_flow_id")
    private String virtualTransFlowId;
    /**
    * 批次号
    */
    @TableField("batch_no")
    private String batchNo;
    /**
    * 单据日期
    */
    @TableField("bill_date")
    private LocalDate billDate;

    /**
     * 平均库龄（天）
     */
    @TableField("avg_inventory_age_days")
    private BigDecimal avgInventoryAgeDays;


    public static final String SKU_ID = "sku_id";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String VIRTUAL_WAREHOUSE_ID = "virtual_warehouse_id";

    public static final String DATE = "date";

    public static final String QTY = "qty";

    public static final String DICT_INVENTORY_STATUS = "dict_inventory_status";

    public static final String VIRTUAL_TRANS_FLOW_ID = "virtual_trans_flow_id";

    public static final String BATCH_NO = "batch_no";

    public static final String BILL_DATE = "bill_date";

    @Override
    public Serializable pkVal() {
        return null;
    }

}