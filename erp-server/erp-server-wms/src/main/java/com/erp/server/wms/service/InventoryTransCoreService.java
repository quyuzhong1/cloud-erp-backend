package com.erp.server.wms.service;

import com.erp.model.wms.dto.inventory.*;
import com.erp.model.wms.enums.inventory.InventoryBizTypeEnum;

/**
 * @Classname: InventoryCoreService
 * @Description: 库存交易核心服务类【业务调用入口】
 * @CreateTime: 2023-05-06  09:28
 * @Author: zhangchunlin
 * Update by：Edison 2023-09-26
 */
public interface InventoryTransCoreService {

    /**
     * 入库/出库 审核，按业务类型 自动匹配库存规则
     * @param dto 出入库业务参数
     */
    void approveByType(InventoryInOutStockDTO dto);

    /**
     * 调拨业务 审核，按业务类型 自动匹配库存规则
     * @param dto 调拨业务参数
     */
    void approveByType(InventoryTransferDTO dto);

    /**
     * 出入库业务审核，出入库业务参数
     * @param dto 业务参数与规则参数
     */
    void approveByRule(InventoryInOutStockRuleDTO dto);

    /**
     * 调拨业务 审核，自定义库存规则
     * @param dto 调拨业务与规则参数
     */
    void approveByRule(InventoryTransferRuleDTO dto);

    /**
     * 单据 反审核
     * @param dto 业务参数
     */
    void unApprove(InventoryUnApproveDTO dto);

    /**
     * 批量反审核
     * @param dto 业务参数
     */
    void batchUnApprove(InventoryBatchUnApproveDTO dto);


}
