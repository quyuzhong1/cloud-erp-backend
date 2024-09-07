package com.erp.server.mrp.calculation.service;

import com.erp.model.mrp.dto.CfgRuleStockUpDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;

import java.util.List;

public interface InventoryService {

    /**
     * 计算FBA可用库存
     * @param replenishmentResultDTO 参数
     * @param codes 选中的code值
     */
    int getFbaUsable(ReplenishmentResultDTO replenishmentResultDTO, List<String> codes);
    /**
     * 计算FBA可用库存
     *
     * @param replenishmentResultDTO 参数
     * @param code                   选中的code值
     * @param stockUpResult
     */
    int getFbaInTransit(ReplenishmentResultDTO replenishmentResultDTO, String code, CfgRuleStockUpDTO.StrategyResultDTO stockUpResult);

    /**
     * 获取历史库存
     * @param replenishmentResult 补货结果
     */
    void getHistoryInventory(ReplenishmentResultDTO replenishmentResult);
}
