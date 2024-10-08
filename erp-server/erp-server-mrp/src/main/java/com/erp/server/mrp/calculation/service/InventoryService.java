package com.erp.server.mrp.calculation.service;

import com.erp.model.mrp.dto.CfgRuleStockUpDTO;
import com.erp.model.mrp.dto.CfgRuleStrategyDTO;
import com.erp.model.mrp.dto.ReplenishmentResultDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface InventoryService {

    /**
     * 计算FBA可用库存
     *
     * @param replenishmentResultDTO 参数
     * @param codes                  选中的code值
     */
    int getFbaUsable(ReplenishmentResultDTO replenishmentResultDTO, Set<String> codes);

    /**
     * 计算FBA可用库存
     *
     * @param replenishmentResultDTO 参数
     * @param code                   选中的code值
     * @param stockUpResult
     */
    int getFbaInTransit(ReplenishmentResultDTO replenishmentResultDTO, String code, CfgRuleStockUpDTO.StrategyResultDTO stockUpResult);

    /**
     * 获取FBA预计发货
     *
     * @param replenishmentResultDTO 参数
     * @param strategyCodes          编码
     * @param stockUpResult          备货配置
     */
    List<ReplenishmentResultDTO.EstimatedDeliveryDetailDTO> getFbaPlanDelivery(ReplenishmentResultDTO replenishmentResultDTO, Set<String> strategyCodes, CfgRuleStockUpDTO.StrategyResultDTO stockUpResult);

    /**
     * 获取海外仓可用库存
     *
     * @param replenishmentResultDTO 参数
     * @param codes                  编码
     * @param cfgRuleStrategyDTO     配置
     */
    int getOverseasUsable(ReplenishmentResultDTO replenishmentResultDTO, Set<String> codes, CfgRuleStrategyDTO cfgRuleStrategyDTO);

    /**
     * 获取本地仓可用库存
     *
     * @param replenishmentResultDTO 参数
     * @param codes                  编码
     * @param cfgRuleStrategyDTO     配置
     */
    int getLocalUsable(ReplenishmentResultDTO replenishmentResultDTO, Set<String> codes, CfgRuleStrategyDTO cfgRuleStrategyDTO);

    /**
     * 获取本地仓在途
     *
     * @param replenishmentResultDTO 参数
     * @param codes                  编码
     * @param cfgRuleStrategyDTO     配置
     */
    int getLocalInTransit(ReplenishmentResultDTO replenishmentResultDTO, Set<String> codes, CfgRuleStrategyDTO cfgRuleStrategyDTO);

    /**
     * 获取本地仓在途
     *
     * @param replenishmentResultDTO 参数
     * @param cfgRuleStrategyDTO     配置
     */
    int getLocalPurchase(ReplenishmentResultDTO replenishmentResultDTO, CfgRuleStrategyDTO cfgRuleStrategyDTO);


    /**
     * 保存所有每日库存到对应库存历史表
     *
     * @param calculationDate 计算日期
     * @param calcDate 计算日期
     */
    void saveAllHistoryInventory(LocalDate calculationDate, String calcDate);

    /**
     * 判断表是否存在
     * @param calculationDate 计算日
     */
    void checkAllTableExists(String calcDate);
}
