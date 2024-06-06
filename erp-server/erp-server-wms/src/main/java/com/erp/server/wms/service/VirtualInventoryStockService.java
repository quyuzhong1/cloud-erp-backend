package com.erp.server.wms.service;

import com.erp.model.wms.dto.inventory.InventoryUnApproveDTO;
import com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO;
import com.erp.model.wms.dto.inventory.VirtualTransRuleDTO;
import com.erp.model.wms.enums.inventory.VirtualInventoryBusinessTypeEnum;

import java.util.List;

/**
 * 虚拟库存变更处理
 * @author will
 * @date 2024/6/4 10:50
 */
public interface VirtualInventoryStockService {

    /**
     * 审核业务
     * @author will
     * @date 2024/6/4 11:12
     * @param paramList 业务参数
     * @param ruleList  规则类参数
     * @param businessType 规则类型
     * @param byType    是否按业务类型 自动匹配规则
     */
    <T extends VirtualInventoryStockDTO.StockBaseDTO> void approve(List<T> paramList, List<VirtualTransRuleDTO.StockParamDTO> ruleList, VirtualInventoryBusinessTypeEnum businessType, Boolean byType);

    /**
     * 反审核业务
     * @author will
     * @date 2024/6/4 11:14
     * @param paramDTO  业务参数
     */
    void unApprove(InventoryUnApproveDTO paramDTO);

}
