package com.erp.model.mrp.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CfgRuleStrategyDTO {

    /**
     * 备货
     */
    private CfgRuleStockUpDTO.StrategyResultDTO stockUpResult;
    /**
     * 销量
     */
    private CfgRuleSalesQtyDTO.StrategyResultDTO salesQtyResult;
    /**
     * 库存
     */
    private List<CfgRuleCommonDTO.StrategyResultDTO> inventoryResult;
    /**
     * 建议
     */
    private List<CfgRuleCommonDTO.StrategyResultDTO> suggestAmountResult;
    /**
     * 仓库
     */
    private CfgRuleWarehouseDTO.StrategyResultDTO warehouseResult;
    /**
     * 系统配置
     */
    private List<CfgSettingDTO> settings;
}
