package com.erp.server.wms.service;

import com.erp.model.wms.dto.inventory.*;

/**
 * @Classname: InventoryCoreService
 * @Description: 库存交易核心服务类
 * @CreateTime: 2023-05-06  09:28
 * @Author: zhangchunlin
 */
public interface InventoryTransCoreService {

    /**
     * 出入库业务，按业务类型（走配置的交易规则），操作引起当前仓仓库的库存变化
     * @param dto
     */
    void approveByType(InventoryInOutStockDTO dto);

    /**
     * 调拨业务，按业务类型（走配置的交易规则），操作引起当前仓和目的仓两个仓库的库存变化
     * @param dto
     */
    void approveByType(InventoryTransferDTO dto);

    /**
     * 调拨业务，自定义规则，，操作引起当前仓和目的仓两个仓库的库存变化，数据库配置的规则满足不了，手工传输交易规则
     * @param dto
     */
    void approveByRule(InventoryTransferRuleDTO dto);

    /**
     * 出入库业务，自定义规则，，操作引起当前仓的库存变化，数据库配置的规则满足不了，手工传输交易规则
     * @param dto
     */
    void approveByRule(InventoryInOutStockRuleDTO dto);

    /**
     * 反审核
     * @param dto
     */
    void unApprove(InventoryUnApproveDTO dto);

    /**
     * 批量反审核
     * @param dto
     */
    void batchUnApprove(InventoryBatchUnApproveDTO dto);


}
