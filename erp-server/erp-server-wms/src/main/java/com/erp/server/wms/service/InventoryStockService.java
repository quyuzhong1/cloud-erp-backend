package com.erp.server.wms.service;

import com.erp.model.wms.dto.inventory.InventoryStockBaseDTO;
import com.erp.model.wms.dto.inventory.InventoryUnApproveDTO;
import com.erp.model.wms.dto.inventory.TransactionRuleDTO;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;

import java.util.List;

/**
 * @Classname: InventoryStockService

 * @CreateTime: 2023-05-04  16:09
 * @Author: zhangchunlin
 */
public interface InventoryStockService {

    /**
     * 审核业务
     * @param paramList         业务参数
     * @param ruleList          规则参数
     * @param businessType      业务类型
     * @param byType            是否按业务类型 自动匹配规则
     * @param <T>               业务参数对象类型
     */
    <T extends InventoryStockBaseDTO> void approve(List<T> paramList, List<TransactionRuleDTO> ruleList, InventoryBusinessTypeEnum businessType, Boolean byType);

    /**
     * 反审核业务
     * @param busiParam 业务参数
     */
    void unApprove(InventoryUnApproveDTO busiParam);

}
