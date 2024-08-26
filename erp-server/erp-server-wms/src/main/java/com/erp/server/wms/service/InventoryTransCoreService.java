package com.erp.server.wms.service;

import com.erp.model.wms.dto.inventory.*;

/**
 * 库存交易核心服务类【业务调用入口】
 * @since 2023-05-06  09:28
 * @author zhangchunlin
 * Update by：Edison 2024-06-28
 */
public interface InventoryTransCoreService {
    String BUSINESS_TYPE = "INVENTORY_TRANS_CORE_SERVICE";

    /**
     * 按业务类型审核: 入库/出库， 自动匹配库存规则
     * @param busiParam 出入库业务参数
     */
    void approveByType(InventoryInOutStockDTO busiParam);

    /**
     * 按业务类型审核: 调拨业务，自动匹配库存规则
     * @param busiParam 调拨业务参数
     */
    void approveByType(InventoryTransferDTO busiParam);

    /**
     * 按自定义规则审核: 出入库业务
     * @param busiParam 业务参数与规则参数
     */
    void approveByRule(InventoryInOutStockRuleDTO busiParam);

    /**
     * 按自定义规则审核: 调拨业务
     * @param busiParam 调拨业务与规则参数
     */
    void approveByRule(InventoryTransferRuleDTO busiParam);

    /**
     * 反审核：业务单据
     * @param busiParam 业务参数
     */
    void unApprove(InventoryUnApproveDTO busiParam);

    /**
     * 批量反审核：业务单据
     * @param busiParam 业务参数
     */
    void batchUnApprove(InventoryBatchUnApproveDTO busiParam);


}
