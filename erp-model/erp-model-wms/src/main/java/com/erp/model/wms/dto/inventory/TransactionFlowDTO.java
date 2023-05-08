package com.erp.model.wms.dto.inventory;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * @Classname: TransactionFlowDTO
 * @Description: TODO
 * @CreateTime: 2023-04-26  18:53
 * @Author: zhangchunlin
 */
@Data
public class TransactionFlowDTO implements Serializable {

    /**
     * 实时库存表id
     */
    private String inventoryId;

    /**
     * 库存明细表id
     */
    private String inventoryDetailId;

    /**
     * 组织id
     */
    private String orgId;

    /**
     * 仓库id
     */
    private String warehouseId;

    /**
     * 仓库名称
     */
    private String warehouseName;

    /**
     * 库位id
     */
    private String warehouseLocation;

    /**
     * 库位名称
     */
    private String warehouseLocationName;


    /**
     * 库存状态
     */
    private String dictInventoryStatus;

    /**
     * 批次日期;关联inventory_detail表的批次日期
     */
    private LocalDate instockBatchDate;


    /**
     * 单据日期
     */
    private LocalDate billDate;


    /**
     * sku id
     */
    private String skuId;

    /**
     * sku编码
     */
    private String skuNo;

    /**
     * 来源单据类型
     */
    private String sourceType;

    /**
     * 来源单据编号
     */
    private String sourceCode;

    /**
     * 来源单据id
     */
    private String sourceId;

    /**
     * 原单明细表id
     */
    private String sourceDetailId;

    /**
     * 业务类型
     */
    private String dictBizType;


    /**
     * 操作数量，出库用负数，入库用正数
     */
    private Integer qty;

    /**
     * 操作后数量
     */
    private Integer curInventoryQty;

    /**
     * 交易规则id
     */
    private String transactionRuleId;

    /**
     * 操作类型
     */
    private String operationMode;

    /**
     * 交易流水号
     */
    private String transactionNo;

}