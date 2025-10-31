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
 * 库存事务表
 * </p>
 *
 * @author shukai
 * @since 2025-10-13
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("inventory_transaction")
public class InventoryTransactionEntity extends BaseEntity<InventoryTransactionEntity> {

    /**
    * 库存表id
    */
    @TableField("inventory_id")
    private String inventoryId;
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
    * 仓库名称
    */
    @TableField("warehouse_name")
    private String warehouseName;
    /**
    * 仓位id
    */
    @TableField("warehouse_location")
    private String warehouseLocation;
    /**
    * 库存状态
    */
    @TableField("dict_inventory_status")
    private String dictInventoryStatus;
    /**
    * 单据日期
    */
    @TableField("bill_date")
    private LocalDate billDate;
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
    * 数量
    */
    @TableField("qty")
    private Integer qty;
    /**
    * 库存流水id
    */
    @TableField("flow_id")
    private String flowId;
    /**
    * 事务id
    */
    @TableField("transaction_id")
    private String transactionId;
    /**
    * 事务类型，global全局事务，local本地事务
    */
    @TableField("transaction_type")
    private String transactionType;

    /**
     * 操作类型
     */
    @TableField("operation_mode")
    private String operationMode;

    public static final String INVENTORY_ID = "inventory_id";

    public static final String ORG_ID = "org_id";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String WAREHOUSE_NAME = "warehouse_name";

    public static final String WAREHOUSE_LOCATION = "warehouse_location";

    public static final String DICT_INVENTORY_STATUS = "dict_inventory_status";

    public static final String BILL_DATE = "bill_date";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String QTY = "qty";

    public static final String FLOW_ID = "flow_id";

    public static final String TRANSACTION_ID = "transaction_id";

    public static final String TRANSACTION_TYPE = "transaction_type";

    @Override
    public Serializable pkVal() {
        return null;
    }

}