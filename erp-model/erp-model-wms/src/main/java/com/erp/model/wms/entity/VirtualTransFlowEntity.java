package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 * <p>
 * 虚拟库存交易流水表
 * </p>
 *
 * @author will
 * @since 2024-06-03
*/
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("virtual_trans_flow")
public class VirtualTransFlowEntity extends BaseEntity<VirtualTransFlowEntity> {

    /**
    * 虚拟库存id
    */
    @TableField("virtual_inventory_id")
    private String virtualInventoryId;
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
    * 虚拟仓库id
    */
    @TableField("virtual_warehouse_id")
    private String virtualWarehouseId;
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
    * 来源单据类型
    */
    @TableField("source_type")
    private String sourceType;
    /**
    * 来源单据id
    */
    @TableField("source_id")
    private String sourceId;
    /**
    * 来源单据编号
    */
    @TableField("source_code")
    private String sourceCode;
    /**
    * 来源单据明细id
    */
    @TableField("source_detail_id")
    private String sourceDetailId;
    /**
    * 业务类型
    */
    @TableField("dict_biz_type")
    private String dictBizType;
    /**
    * 操作用户id
    */
    @TableField("user_id")
    private String userId;
    /**
    * 交易时间
    */
    @TableField("trade_time")
    private LocalDateTime tradeTime;
    /**
    * 数量
    */
    @TableField("qty")
    private Integer qty;
    /**
    * 交易后数量
    */
    @TableField("cur_inventory_qty")
    private Integer curInventoryQty;
    /**
    * 虚拟规则id
    */
    @TableField("virtual_trans_rule_id")
    private String virtualTransRuleId;
    /**
    * 操作类型
    */
    @TableField("operation_mode")
    private String operationMode;
    /**
    * 流水号，一个业务操作关联
    */
    @TableField("transaction_no")
    private String transactionNo;
    /**
    * 是否已经反审核，默认为false
    */
    @TableField("is_unapproved")
    private Boolean isUnapproved;


    public static final String VIRTUAL_INVENTORY_ID = "virtual_inventory_id";

    public static final String ORG_ID = "org_id";

    public static final String WAREHOUSE_ID = "warehouse_id";

    public static final String VIRTUAL_WAREHOUSE_ID = "virtual_warehouse_id";

    public static final String DICT_INVENTORY_STATUS = "dict_inventory_status";

    public static final String BILL_DATE = "bill_date";

    public static final String SKU_ID = "sku_id";

    public static final String SKU_NO = "sku_no";

    public static final String SOURCE_TYPE = "source_type";

    public static final String SOURCE_ID = "source_id";

    public static final String SOURCE_CODE = "source_code";

    public static final String SOURCE_DETAIL_ID = "source_detail_id";

    public static final String DICT_BIZ_TYPE = "dict_biz_type";

    public static final String USER_ID = "user_id";

    public static final String TRADE_TIME = "trade_time";

    

    public static final String CUR_INVENTORY_QTY = "cur_inventory_qty";

    public static final String VIRTUAL_TRANS_RULE_ID = "virtual_trans_rule_id";

    public static final String OPERATION_MODE = "operation_mode";

    public static final String TRANSACTION_NO = "transaction_no";

    public static final String IS_UNAPPROVED = "is_unapproved";

    @Override
    public Serializable pkVal() {
        return null;
    }

}