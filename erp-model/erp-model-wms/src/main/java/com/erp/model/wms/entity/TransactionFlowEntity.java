package com.erp.model.wms.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @Classname: TransactionFlow
 * @Description: 库存交易流水
 * @CreateTime: 2023-04-25  19:28
 * @Author: zhangchunlin
 */
@Getter
@Setter
@Accessors(chain = true)
@TableName("transaction_flow")
public class TransactionFlowEntity extends BaseEntity<TransactionFlowEntity> implements Serializable {

    /**
     * 实时库存表id
     */
    @TableField("inventory_id")
    private String inventoryId;

    /**
     * 库存明细表id
     */
    @TableField("inventory_detail_id")
    private String inventoryDetailId;

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
     * 库位id
     */
    @TableField("warehouse_location")
    private String warehouseLocation;

    /**
     * 库位名称
     */
    @TableField("warehouse_location_name")
    private String warehouseLocationName;


    /**
     * 库存状态
     */
    @TableField("dict_inventory_status")
    private String dictInventoryStatus;

    /**
     * 批次日期;关联inventory_detail表的批次日期
     */
    @TableField("instock_batch_date")
    private LocalDate instockBatchDate;


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
     * sku编码
     */
    @TableField("sku_no")
    private String skuNo;



    /**
     * 来源单据类型
     */
    @TableField("source_type")
    private String sourceType;

    /**
     * 来源单据编号
     */
    @TableField("source_code")
    private String sourceCode;

    /**
     * 来源单据id
     */
    @TableField("source_id")
    private String sourceId;

    /**
     * 原单明细表id
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
     * 操作数量，出库用负数，入库用正数
     */
    @TableField("qty")
    private Integer qty;

    /**
     * 操作后数量
     */
    @TableField("cur_inventory_qty")
    private Integer curInventoryQty;

    /**
     * 交易规则id
     */
    @TableField("transaction_rule_id")
    private String transactionRuleId;

    /**
     * 操作类型
     */
    @TableField("operation_mode")
    private String operationMode;

    /**
     * 交易流水号（一个业务操作关联多个流水号）
     */
    @TableField("transaction_no")
    private String transactionNo;

}