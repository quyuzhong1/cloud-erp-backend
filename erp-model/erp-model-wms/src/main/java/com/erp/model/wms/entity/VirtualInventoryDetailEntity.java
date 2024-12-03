package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.common.business.enums.ApproveStatusEnum;


/**
 * <p>
 * 虚拟仓库明细
 * </p>
 *
 * @author will
 * @since 2024-12-03
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("virtual_inventory_detail")
public class VirtualInventoryDetailEntity extends BaseEntity<VirtualInventoryDetailEntity> {

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
    * skuId
    */
    @TableField("sku_id")
    private String skuId;
    /**
    * SKU编号
    */
    @TableField("sku_no")
    private String skuNo;
    /**
    * 单据日期
    */
    @TableField("bill_date")
    private LocalDate billDate;
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
    * 虚拟仓入库流水id
    */
    @TableField("virtual_trans_flow_id")
    private String virtualTransFlowId;
    /**
    * 批次号
    */
    @TableField("batch_no")
    private String batchNo;
    /**
    * 最后出库日期
    */
    @TableField("last_outstock_date")
    private LocalDate lastOutstockDate;


    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String VIRTUAL_WAREHOUSE_ID = "virtual_warehouse_id";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String BILL_DATE = "bill_date";

    public static final String QTY = "qty";

    public static final String DICT_INVENTORY_STATUS = "dict_inventory_status";

    public static final String VIRTUAL_TRANS_FLOW_ID = "virtual_trans_flow_id";

    public static final String BATCH_NO = "batch_no";

    public static final String LAST_OUTSTOCK_DATE = "last_outstock_date";

    @Override
    public Serializable pkVal() {
        return null;
    }

}