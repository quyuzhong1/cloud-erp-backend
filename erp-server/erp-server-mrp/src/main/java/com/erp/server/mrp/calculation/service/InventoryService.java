package com.erp.server.mrp.calculation.service;

import com.erp.model.mrp.dto.CfgRuleCommonDTO;
import com.erp.model.mrp.dto.CfgRuleStockUpDTO;
import com.erp.model.mrp.dto.CfgRuleStrategyDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;

import java.util.List;

public interface InventoryService {

    /**
     * 计算FBA可用库存
     *
     * @param replenishmentResultDTO 参数
     * @param codes                  选中的code值
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
     *
     * @param replenishmentResult 补货结果
     */
    void getHistoryInventory(ReplenishmentResultDTO replenishmentResult);

    /**
     * 获取FBA预计发货
     *
     * @param replenishmentResultDTO 参数
     * @param strategyCodes          编码
     * @param stockUpResult          备货配置
     */
    void getFbaPlanDelivery(ReplenishmentResultDTO replenishmentResultDTO, List<String> strategyCodes, CfgRuleStockUpDTO.StrategyResultDTO stockUpResult);

    /**
     * 获取海外仓可用库存
     *
     * @param replenishmentResultDTO 参数
     * @param codes                  编码
     * @param cfgRuleStrategyDTO     配置
     */
    int getOverseasUsable(ReplenishmentResultDTO replenishmentResultDTO, List<String> codes, CfgRuleStrategyDTO cfgRuleStrategyDTO);

    /**
     * 获取本地仓可用库存
     * @param replenishmentResultDTO 参数
     * @param codes                  编码
     * @param cfgRuleStrategyDTO     配置
     */
    int getLocalUsable(ReplenishmentResultDTO replenishmentResultDTO, List<String> codes, CfgRuleStrategyDTO cfgRuleStrategyDTO);

    /**
     * 获取本地仓在途
     * @param replenishmentResultDTO 参数
     * @param codes                  编码
     * @param cfgRuleStrategyDTO     配置
     */
    int getLocalInTransit(ReplenishmentResultDTO replenishmentResultDTO, List<String> codes, CfgRuleStrategyDTO cfgRuleStrategyDTO);

    /**
     * 获取本地仓在途
     * @param replenishmentResultDTO 参数
     * @param localPurchase          本地采购
     * @param cfgRuleStrategyDTO     配置
     */
    int getLocalPurchase(ReplenishmentResultDTO replenishmentResultDTO, CfgRuleCommonDTO.StrategyResultDTO localPurchase, CfgRuleStrategyDTO cfgRuleStrategyDTO);
}
