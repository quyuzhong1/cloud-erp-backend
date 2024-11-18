package com.erp.server.mrp.calculation.service;

import com.erp.model.mrp.dto.*;
import com.erp.model.mrp.enums.ReplenishmentInventoryTypeEnum;

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
     * @param stockUpResult          备货配置
     */
    int getFbaInTransit(ReplenishmentResultDTO replenishmentResultDTO, String code, CfgRuleStockUpDTO.StrategyResultDTO stockUpResult);

    /**
     * 获取FBA预计发货
     *
     * @param replenishmentResultDTO 参数
     * @param strategyCodes          编码
     * @param stockUpResult          备货配置
     * @param sourceType             来源类型
     * @param inventoryTypeEnum      库存类型
     * @param type                   类型
     */
    List<ReplenishmentResultDTO.EstimatedDeliveryDetailDTO> getPlanDelivery(ReplenishmentResultDTO replenishmentResultDTO, Set<String> strategyCodes,
                                                                            CfgRuleStockUpDTO.StrategyResultDTO stockUpResult,
                                                                            String sourceType,
                                                                            ReplenishmentInventoryTypeEnum inventoryTypeEnum,
                                                                            String type);

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
     * @param calcDate        计算日期
     */
    void saveAllHistoryInventory(LocalDate calculationDate, String calcDate);

    /**
     * 判断表是否存在
     *
     * @param calcDate 计算日
     */
    void checkAllTableExists(String calcDate);

    /**
     * 根据建议配置获取库存
     *
     * @param replenishmentResultDTO  建议
     * @param endDate                 结束时间
     * @param deliveryVolumeInventory 库存建议配置
     * @param warehouseResult         仓库配置
     */
    int getInventory(ReplenishmentResultDTO replenishmentResultDTO, LocalDate endDate, Set<String> deliveryVolumeInventory, CfgRuleWarehouseDTO.StrategyResultDTO warehouseResult);

    /**
     * 补货计划
     *
     * @param replenishmentResultDTO 建议
     * @param replenishmentPlan      单据状态
     * @param stockUpResult          备货配置
     * @param inventoryTypeEnum      库存类型
     */
    List<ReplenishmentResultDTO.EstimatedDeliveryDetailDTO> getReplenishmentPlan(ReplenishmentResultDTO replenishmentResultDTO, Set<String> replenishmentPlan,
                                                                                 CfgRuleStockUpDTO.StrategyResultDTO stockUpResult, ReplenishmentInventoryTypeEnum inventoryTypeEnum);

    /**
     * 计算海外在途库存
     *
     * @param replenishmentResultDTO 参数
     * @param code                   选中的code值
     * @param cfgRuleStrategyDTO     配置
     */
    int getOverseasInTransit(ReplenishmentResultDTO replenishmentResultDTO, String code, CfgRuleStrategyDTO cfgRuleStrategyDTO);


    /**
     * 计算海外预计发货库存
     * @param replenishmentResultDTO 建议
     * @param cfgRuleStrategyDTO     配置
     */
    int getOverseasPlanDelivery(ReplenishmentResultDTO replenishmentResultDTO, CfgRuleStrategyDTO cfgRuleStrategyDTO);

}
